/*
 * This software is licensed under the MIT License
 * https://github.com/GStefanowich/MC-Server-Protection
 *
 * Copyright (c) 2019 Gregory Stefanowich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.TheElm.project.commands.ArgumentTypes;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.TheElm.project.interfaces.BoolEnums;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EnumArgumentType<E extends Enum<E>> implements ArgumentType<Enum<E>> {
    public static final DynamicCommandExceptionType INVALID_COMPONENT_EXCEPTION = new DynamicCommandExceptionType((object_1) -> new TranslatableText("argument.component.invalid", object_1));
    private final EnumSet<E> enumValues;
    
    private EnumArgumentType( final Class<E> enumClass ) {
        this.enumValues = EnumSet.allOf( enumClass );
    }
    
    public static EnumArgumentType getEnumArguments(CommandContext<ServerCommandSource> commandContext, String string) {
        return commandContext.getArgument(string, EnumArgumentType.class);
    }
    
    public static <T extends Enum<T>> EnumArgumentType<T> create( Class<T> enumClass ) {
        return new EnumArgumentType<>( enumClass );
    }
    
    public static <T extends Enum<T>> T getEnum(Class<T> tClass, String search) throws CommandSyntaxException {
        return EnumSet.allOf( tClass ).stream().filter((enumValue) -> {
            return enumValue.name().equalsIgnoreCase( search );
        }).findFirst().orElseThrow(() -> INVALID_COMPONENT_EXCEPTION.create(search));
    }
    
    @Override
    public Enum<E> parse(StringReader reader) throws CommandSyntaxException {
        final String check = reader.readUnquotedString();
        Optional<E> findVal = this.enumValues.stream().filter((enumValue) -> enumValue.name().equals( check )).findFirst();
        return findVal.orElse(null);
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder suggestionsBuilder) {
        return CommandSource.suggestMatching(
            this.enumValues.stream()
                .filter((val) -> ((!(val instanceof BoolEnums)) || ((BoolEnums)val).isEnabled()))
                .map((enumValue) -> enumValue.name().toLowerCase()),
            suggestionsBuilder
        );
    }
    
}
