package org.jfree.chart.renderer;

import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.util.*;

import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class ShapeManager {

    /** The default shape. */
    public static final Shape DEFAULT_SHAPE
            = new Rectangle2D.Double(-3.0, -3.0, 6.0, 6.0);

    /** A shape list. */
    protected ShapeList shapeList;

    /**
     * A flag that controls whether or not the shapeList is auto-populated
     * in the {@link #lookupSeriesShape(int)} method.
     *
     * @since 1.0.6
     */
    protected boolean autoPopulateSeriesShape;

    /** The base shape. */
    protected transient Shape defaultShape;

    /** Storage for registered change listeners. */
    protected transient EventListenerList listenerList;

    /** An event for re-use. */
    protected transient RendererChangeEvent event;

    public ShapeManager() {
        this.shapeList = new ShapeList();
        this.defaultShape = DEFAULT_SHAPE;
        this.autoPopulateSeriesShape = true;

        this.listenerList = new EventListenerList();
    }

    // SHAPE

    /**
     * Returns the drawing supplier from the plot.
     *
     * @return The drawing supplier.
     */
    public abstract DrawingSupplier getDrawingSupplier();

    /**
     * Returns a shape used to represent a data item.
     * <p>
     * The default implementation passes control to the
     * {@link #lookupSeriesShape(int)} method. You can override this method if
     * you require different behaviour.
     *
     * @param row  the row (or series) index (zero-based).
     * @param column  the column (or category) index (zero-based).
     *
     * @return The shape (never {@code null}).
     */
    public Shape getItemShape(int row, int column) {
        return lookupSeriesShape(row);
    }

    /**
     * Returns a shape used to represent the items in a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The shape (never {@code null}).
     *
     * @since 1.0.6
     */
    public Shape lookupSeriesShape(int series) {

        Shape result = getSeriesShape(series);
        if (result == null && this.autoPopulateSeriesShape) {
            DrawingSupplier supplier = getDrawingSupplier();
            if (supplier != null) {
                result = supplier.getNextShape();
                setSeriesShape(series, result, false);
            }
        }
        if (result == null) {
            result = this.defaultShape;
        }
        return result;

    }

    /**
     * Returns a shape used to represent the items in a series.
     *
     * @param series  the series (zero-based index).
     *
     * @return The shape (possibly {@code null}).
     *
     * @see #setSeriesShape(int, Shape)
     */
    public Shape getSeriesShape(int series) {
        return this.shapeList.getShape(series);
    }

    /**
     * Sets the shape used for a series and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param shape  the shape ({@code null} permitted).
     *
     * @see #getSeriesShape(int)
     */
    public void setSeriesShape(int series, Shape shape) {
        setSeriesShape(series, shape, true);
    }

    /**
     * Sets the shape for a series and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index (zero based).
     * @param shape  the shape ({@code null} permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesShape(int)
     */
    public void setSeriesShape(int series, Shape shape, boolean notify) {
        this.shapeList.setShape(series, shape);
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the default shape.
     *
     * @return The shape (never {@code null}).
     *
     * @see #setDefaultShape(Shape)
     */
    public Shape getDefaultShape() {
        return this.defaultShape;
    }

    /**
     * Sets the default shape and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param shape  the shape ({@code null} not permitted).
     *
     * @see #getDefaultShape()
     */
    public void setDefaultShape(Shape shape) {
        // defer argument checking...
        setDefaultShape(shape, true);
    }

    /**
     * Sets the default shape and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param shape  the shape ({@code null} not permitted).
     * @param notify  notify listeners?
     *
     * @see #getDefaultShape()
     */
    public void setDefaultShape(Shape shape, boolean notify) {
        Args.nullNotPermitted(shape, "shape");
        this.defaultShape = shape;
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the flag that controls whether or not the series shape list is
     * automatically populated when {@link #lookupSeriesShape(int)} is called.
     *
     * @return A boolean.
     *
     * @since 1.0.6
     *
     * @see #setAutoPopulateSeriesShape(boolean)
     */
    public boolean getAutoPopulateSeriesShape() {
        return this.autoPopulateSeriesShape;
    }

    /**
     * Sets the flag that controls whether or not the series shape list is
     * automatically populated when {@link #lookupSeriesShape(int)} is called.
     *
     * @param auto  the new flag value.
     *
     * @since 1.0.6
     *
     * @see #getAutoPopulateSeriesShape()
     */
    public void setAutoPopulateSeriesShape(boolean auto) {
        this.autoPopulateSeriesShape = auto;
    }


    /**
     * Sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @since 1.0.5
     */
    protected void fireChangeEvent() {

        // the commented out code would be better, but only if
        // RendererChangeEvent is immutable, which it isn't.  See if there is
        // a way to fix this...

        //if (this.event == null) {
        //    this.event = new RendererChangeEvent(this);
        //}
        //notifyListeners(this.event);

        notifyListeners(new RendererChangeEvent(this));
    }


    /**
     * Notifies all registered listeners that the renderer has been modified.
     *
     * @param event  information about the change event.
     */
    public void notifyListeners(RendererChangeEvent event) {
        Object[] ls = this.listenerList.getListenerList();
        for (int i = ls.length - 2; i >= 0; i -= 2) {
            if (ls[i] == RendererChangeListener.class) {
                ((RendererChangeListener) ls[i + 1]).rendererChanged(event);
            }
        }
    }


    /**
     * Tests this renderer for equality with another object.
     *
     * @param obj  the object ({@code null} permitted).
     *
     * @return {@code true} or {@code false}.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ShapeManager)) {
            return false;
        }
        ShapeManager that = (ShapeManager) obj;
        if (!ObjectUtils.equal(this.shapeList, that.shapeList)) {
            return false;
        }
        if (!ShapeUtils.equal(this.defaultShape, that.defaultShape)) {
            return false;
        }
        return true;
    }

    /**
     * Returns an independent copy of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if some component of the renderer
     *         does not support cloning.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        ShapeManager clone = (ShapeManager) super.clone();
        if (this.shapeList != null) {
            clone.shapeList = (ShapeList) this.shapeList.clone();
        }
        if (this.defaultShape != null) {
            clone.defaultShape = ShapeUtils.clone(this.defaultShape);
        }
        clone.listenerList = new EventListenerList();
        clone.event = null;
        return clone;
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writeShape(this.defaultShape, stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.defaultShape = SerialUtils.readShape(stream);

        // listeners are not restored automatically, but storage must be
        // provided...
        this.listenerList = new EventListenerList();
    }

}
