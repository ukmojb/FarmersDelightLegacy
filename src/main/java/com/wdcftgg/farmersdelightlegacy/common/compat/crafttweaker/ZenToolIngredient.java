package com.wdcftgg.farmersdelightlegacy.common.compat.crafttweaker;

import com.wdcftgg.farmersdelightlegacy.common.recipe.ToolAction;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.CuttingBoardRecipeManager.IngredientMatcher;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientAny;
import crafttweaker.api.item.IngredientStack;
import crafttweaker.api.item.IItemCondition;
import crafttweaker.api.item.IItemTransformer;
import crafttweaker.api.item.IItemTransformerNew;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ZenRegister
@ZenClass("mods.farmersdelight.ToolIngredient")
public final class ZenToolIngredient extends IngredientStack {
    private final Predicate<ItemStack> matcher;
    private final Supplier<List<ItemStack>> display;
    private final String command;
    private final IIngredient conditions;

    private ZenToolIngredient(Predicate<ItemStack> matcher, Supplier<List<ItemStack>> display, String command) {
        this(matcher, display, command, IngredientAny.INSTANCE);
    }

    private ZenToolIngredient(Predicate<ItemStack> matcher, Supplier<List<ItemStack>> display, String command, IIngredient conditions) {
        super(IngredientAny.INSTANCE, 1);
        this.matcher = matcher;
        this.display = display;
        this.command = command;
        this.conditions = conditions;
    }

    @ZenMethod
    public static IIngredient of(String action) {
        try {
            ToolAction tool = new ToolAction(action);
            return new ZenToolIngredient(tool::matches, tool::getDisplayStacks,
                    "mods.farmersdelight.ToolIngredient.of(" + RecipeScript.quote(action) + ")");
        } catch (IllegalArgumentException exception) {
            crafttweaker.CraftTweakerAPI.logError(exception.getMessage());
            throw exception;
        }
    }

    @ZenMethod
    public static IIngredient ore(String name) {
        if (name == null || name.isEmpty()) {
            crafttweaker.CraftTweakerAPI.logError("Tool ore dictionary name must not be empty");
            throw new IllegalArgumentException("Tool ore dictionary name must not be empty");
        }
        IngredientMatcher tool = IngredientMatcher.fromTokens(new String[]{"ore:" + name}, "farmersdelight");
        return new ZenToolIngredient(tool::matchesTool, tool::asStacks,
                "mods.farmersdelight.ToolIngredient.ore(" + RecipeScript.quote(name) + ")");
    }

    @Override
    public boolean matches(IItemStack stack) {
        return matcher.test(CraftTweakerCompatHelper.stackOf(stack)) && conditions.matches(stack);
    }

    @Override
    public boolean matchesExact(IItemStack stack) {
        return matches(stack);
    }

    @Override
    public List<IItemStack> getItems() {
        return java.util.Arrays.asList(CraftTweakerMC.getIItemStacks(display.get()));
    }

    @Override
    public IItemStack[] getItemArray() {
        return getItems().toArray(new IItemStack[0]);
    }

    @Override
    public Object getInternal() {
        return this;
    }

    @Override
    public String toCommandString() {
        if (conditions != IngredientAny.INSTANCE) {
            crafttweaker.CraftTweakerAPI.logError("Tool ingredient callback conditions cannot be exported as source code");
            throw new IllegalStateException("Tool ingredient callback conditions cannot be exported as source code");
        }
        return command;
    }

    @Override
    public IIngredient amount(int amount) {
        return new IngredientStack(this, amount);
    }

    @Override
    public IIngredient only(IItemCondition condition) {
        return new ZenToolIngredient(matcher, display, command, conditions.only(condition));
    }

    @Override
    public IIngredient marked(String mark) {
        return new ZenToolIngredient(matcher, display, command, conditions.marked(mark));
    }

    @Override
    public String getMark() {
        return conditions.getMark();
    }

    @Override
    public IIngredient transformNew(IItemTransformerNew transformer) {
        return new ZenToolIngredient(matcher, display, command, conditions.transformNew(transformer));
    }

    @Override
    public IIngredient transform(IItemTransformer transformer) {
        return new ZenToolIngredient(matcher, display, command, conditions.transform(transformer));
    }

    @Override
    public boolean hasTransformers() {
        return conditions.hasTransformers();
    }

    @Override
    public boolean hasNewTransformers() {
        return conditions.hasNewTransformers();
    }

    @Override
    public IItemStack applyTransform(IItemStack stack, IPlayer player) {
        return conditions.applyTransform(stack, player);
    }

    @Override
    public IItemStack applyNewTransform(IItemStack stack) {
        return conditions.applyNewTransform(stack);
    }

    @Override
    public boolean contains(IIngredient ingredient) {
        IItemStack[] stacks = ingredient.getItemArray();
        return stacks.length > 0 && java.util.Arrays.stream(stacks).allMatch(this::matches);
    }
}
