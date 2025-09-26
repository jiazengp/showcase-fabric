package com.showcase.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.showcase.utils.TimeUtils;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Custom argument type for parsing time durations
 * Supports formats like "1h", "30m", "1h30m", "90s", or plain numbers
 */
public class TimeArgumentType implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("30", "1m", "5m", "1h", "1h30m");
    private static final SimpleCommandExceptionType INVALID_TIME = new SimpleCommandExceptionType(
            Text.translatable("showcase.command.invalid_time_format")
    );

    private final int minSeconds;
    private final int maxSeconds;

    private TimeArgumentType(int minSeconds, int maxSeconds) {
        this.minSeconds = minSeconds;
        this.maxSeconds = maxSeconds;
    }

    public static TimeArgumentType time() {
        return new TimeArgumentType(1, Integer.MAX_VALUE);
    }

    public static TimeArgumentType time(int min, int max) {
        return new TimeArgumentType(min, max);
    }

    public static int getTime(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String timeString = readTimeString(reader);

        try {
            int seconds = TimeUtils.parseTimeToSeconds(timeString);

            // Validate bounds
            if (seconds < minSeconds || seconds > maxSeconds) {
                reader.setCursor(start);
                throw new SimpleCommandExceptionType(
                        Text.translatable("showcase.command.time_out_of_range", minSeconds, maxSeconds)
                ).createWithContext(reader);
            }

            return seconds;
        } catch (IllegalArgumentException e) {
            reader.setCursor(start);
            throw INVALID_TIME.createWithContext(reader);
        }
    }

    private String readTimeString(StringReader reader) {
        int start = reader.getCursor();
        while (reader.canRead() && isValidTimeChar(reader.peek())) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    private boolean isValidTimeChar(char c) {
        return Character.isDigit(c) || c == 'h' || c == 'm' || c == 's' || c == 'H' || c == 'M' || c == 'S';
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();

        // Suggest common time formats
        for (String example : EXAMPLES) {
            if (example.startsWith(input)) {
                builder.suggest(example);
            }
        }

        // Add some contextual suggestions based on input
        if (input.isEmpty()) {
            builder.suggest("30s");
            builder.suggest("5m");
            builder.suggest("1h");
        } else if (input.matches("\\d+")) {
            // If user typed just numbers, suggest time units
            builder.suggest(input + "s");
            builder.suggest(input + "m");
            if (Integer.parseInt(input) <= 24) { // Only suggest hours for reasonable values
                builder.suggest(input + "h");
            }
        } else if (input.matches("\\d+h")) {
            // If user typed hours, suggest adding minutes
            builder.suggest(input + "30m");
        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public String toString() {
        return "time()";
    }
}