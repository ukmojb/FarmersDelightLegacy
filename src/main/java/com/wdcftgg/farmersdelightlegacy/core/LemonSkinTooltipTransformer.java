package com.wdcftgg.farmersdelightlegacy.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class LemonSkinTooltipTransformer implements IClassTransformer {
    private static final Logger LOGGER = LogManager.getLogger("FarmersDelightTooltipCompat");
    private static final String HOOK = "com/wdcftgg/farmersdelightlegacy/client/compat/LemonSkinTooltip";
    private static final String VALUES = "Lua/myxazaur/lemonskin/helpers/FoodHelper$BasicFoodValues;";
    private static final String STACK = "Lnet/minecraft/item/ItemStack;";
    private static final String POST_TEXT = "Lnet/minecraftforge/client/event/RenderTooltipEvent$PostText;";

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        boolean helper = "ua.myxazaur.lemonskin.helpers.TooltipHelper".equals(transformedName);
        boolean renderer = "ua.myxazaur.lemonskin.client.TooltipOverlayHandler".equals(transformedName);
        if (!helper && !renderer) {
            return bytes;
        }
        try {
            ClassNode node = new ClassNode();
            new ClassReader(bytes).accept(node, 0);
            if (helper) {
                delegate(requireMethod(node, "reserveFoodTooltipSpace", "(Ljava/util/List;" + STACK + ")V"));
            } else {
                delegate(requireMethod(node, "renderModern", "(" + POST_TEXT + STACK + VALUES + VALUES + ")V"));
                updateEmptyFoodCheck(requireMethod(node, "onRenderTooltip", "(" + POST_TEXT + ")V"));
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            LOGGER.info("已修正 LemonSkin 提示布局：{}", transformedName);
            return writer.toByteArray();
        } catch (RuntimeException exception) {
            LOGGER.error("无法修正 LemonSkin 提示布局，目标类：{}", transformedName, exception);
            throw exception;
        }
    }

    private static MethodNode requireMethod(ClassNode node, String name, String descriptor) {
        for (MethodNode method : node.methods) {
            if (name.equals(method.name) && descriptor.equals(method.desc)) {
                return method;
            }
        }
        throw new IllegalStateException("LemonSkin 方法签名不匹配：" + node.name + "." + name + descriptor);
    }

    private static void delegate(MethodNode method) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) {
            method.localVariables.clear();
        }
        method.visibleLocalVariableAnnotations = null;
        method.invisibleLocalVariableAnnotations = null;
        int local = (method.access & Opcodes.ACC_STATIC) == 0 ? 1 : 0;
        for (Type argument : Type.getArgumentTypes(method.desc)) {
            method.instructions.add(new VarInsnNode(argument.getOpcode(Opcodes.ILOAD), local));
            local += argument.getSize();
        }
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, method.name, method.desc, false));
        method.instructions.add(new InsnNode(Opcodes.RETURN));
    }

    private static void updateEmptyFoodCheck(MethodNode method) {
        int replaced = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if ("ua/myxazaur/lemonskin/helpers/FoodHelper$BasicFoodValues".equals(call.owner)
                        && "equals".equals(call.name) && "(Ljava/lang/Object;)Z".equals(call.desc)) {
                    method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK,
                            "canSkipTooltip", "(" + VALUES + VALUES + ")Z", false));
                    replaced++;
                }
            }
        }
        if (replaced != 1) {
            throw new IllegalStateException("LemonSkin 零恢复值判定结构不匹配：" + replaced);
        }
    }
}
