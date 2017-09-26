package me.towdium.jecalculation.client.gui.drawables;

import mcp.MethodsReturnNonnullByDefault;
import me.towdium.jecalculation.client.gui.IDrawable;
import me.towdium.jecalculation.client.gui.JecGui;
import me.towdium.jecalculation.core.labels.ILabel;
import me.towdium.jecalculation.utils.wrappers.Single;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Author: towdium
 * Date:   17-8-17.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DLabelGroup implements IDrawable {
    ArrayList<DLabel> widgets = new ArrayList<>();

    public DLabelGroup(int xPos, int yPos, int column, int row, DLabel.enumMode mode) {
        this(xPos, yPos, column, row, 18, 18, mode);
    }

    public DLabelGroup(int xPos, int yPos, int column, int row, int xSize, int ySize, DLabel.enumMode mode) {
        IntStream.range(0, row).forEach(r -> IntStream.range(0, column).forEach(c ->
                widgets.add(new DLabel(xPos + c * xSize, yPos + r * ySize, xSize, ySize, mode))));
    }

    @Override
    public void onDraw(JecGui gui, int xMouse, int yMouse) {
        widgets.forEach(w -> w.onDraw(gui, xMouse, yMouse));
    }

    @Override
    public boolean onClicked(JecGui gui, int xMouse, int yMouse, int button) {
        return widgets.stream().anyMatch(w -> w.onClicked(gui, xMouse, yMouse, button));
    }

    public Optional<DLabel> getEntryAt(int xMouse, int yMouse) {
        return widgets.stream().filter(w -> w.mouseIn(xMouse, yMouse)).findFirst();
    }

    public void setLabel(List<ILabel> labels, int start) {
        Single<Integer> i = new Single<>(start);
        widgets.forEach(l -> l.setLabel(i.value < labels.size() ? labels.get(i.value++) : ILabel.EMPTY));
    }
}