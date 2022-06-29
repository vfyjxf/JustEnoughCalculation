package me.towdium.jecalculation.data.structure;

import me.towdium.jecalculation.data.label.ILabel;
import me.towdium.jecalculation.utils.Utilities;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author: towdium
 * Date:   17-10-6.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Recipe {
    public static final String KEY_INPUT = "input";
    public static final String KEY_CATALYST = "catalyst";
    public static final String KEY_OUTPUT = "output";
    List<ILabel> input;
    List<ILabel> catalyst;
    List<ILabel> output;

    public Recipe(CompoundTag nbt) {
        this(readNbtList(nbt.getList(KEY_INPUT, 10)),
                readNbtList(nbt.getList(KEY_CATALYST, 10)),
                readNbtList(nbt.getList(KEY_OUTPUT, 10)));
    }

    public Recipe(List<ILabel> input, List<ILabel> catalyst, List<ILabel> output) {
        boolean a = Stream.of(input, output, catalyst).anyMatch(i ->
                !i.isEmpty() && i.get(i.size() - 1) == ILabel.EMPTY);
        boolean b = Stream.of(input, output).anyMatch(i ->
                i.stream().allMatch(j -> j == ILabel.EMPTY));
        if (a || b) throw new IllegalArgumentException("Invalid recipe");
        this.input = input;
        this.catalyst = catalyst;
        this.output = output;
    }

    static private List<ILabel> readNbtList(ListTag list) {
        return list.stream().filter(n -> n instanceof CompoundTag)
                .map(n -> ILabel.SERIALIZER.deserialize((CompoundTag) n))
                .collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        int[] hash = new int[1];
        Consumer<List<ILabel>> hasher = (ls) -> ls.stream()
                .filter(Objects::nonNull).forEach(i -> hash[0] ^= i.hashCode());
        hasher.accept(input);
        hasher.accept(catalyst);
        hasher.accept(output);
        return hash[0];
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Recipe)) return false;
        Recipe r = (Recipe) obj;
        BiPredicate<List<ILabel>, List<ILabel>> p = (i, j) -> {
            if (i.size() != j.size()) return false;
            for (int k = 0; k < i.size(); k++)
                if (!i.get(k).equals(j.get(k))) return false;
            return true;
        };
        return p.test(input, r.input) && p.test(catalyst, r.catalyst) && p.test(output, r.output);
    }

    public List<ILabel> getLabel(IO type) {
        return get(type, input, output, catalyst);
    }

    public static <T> T get(IO type, T input, T output, T catalyst) {
        switch (type) {
            case INPUT:
                return input;
            case OUTPUT:
                return output;
            case CATALYST:
                return catalyst;
            default:
                throw new RuntimeException("Internal error");
        }
    }

    public ILabel getRep() {
        for (int i = 0; i < 8; i++)
            if (output.get(i) != ILabel.EMPTY) return output.get(i);
        return ILabel.EMPTY;
    }

    public CompoundTag serialize() {
        CompoundTag ret = new CompoundTag();
        Function<List<ILabel>, ListTag> convert = (ls) -> {
            ArrayList<ILabel> labels = new ArrayList<>();
            boolean start = false;
            for (int i = ls.size() - 1; i >= 0; i--) {
                if (start || ls.get(i) != ILabel.EMPTY) {
                    labels.add(ls.get(i));
                    start = true;
                }
            }

            ListTag r = new ListTag();
            new Utilities.ReversedIterator<>(labels).stream()
                    .forEach(l -> r.add(ILabel.SERIALIZER.serialize(l)));
            return r;
        };
        ret.put(KEY_INPUT, convert.apply(input));
        ret.put(KEY_CATALYST, convert.apply(catalyst));
        ret.put(KEY_OUTPUT, convert.apply(output));
        return ret;
    }

    public Optional<ILabel> matches(ILabel label) {
        return output.stream().filter(i -> ILabel.MERGER.merge(label, i).isPresent()).findAny();
    }

    public long multiplier(ILabel label) {
        return output.stream().filter(i -> ILabel.MERGER.merge(label, i).isPresent()).findAny()
                .map(i -> {
                    long amountA = label.getAmount();
                    if (!label.isPercent()) amountA = Math.multiplyExact(amountA, 100L);
                    long amountB = i.getAmount();
                    if (!i.isPercent()) amountB = Math.multiplyExact(amountB, 100L);
                    return (amountB + Math.abs(amountA) - 1) / amountB;
                }).orElse(0L);
    }

    public enum IO {
        INPUT, OUTPUT, CATALYST;

        public static IO isInput(boolean b) {
            return b ? INPUT : OUTPUT;
        }
    }
}
