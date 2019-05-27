package org.jfree.chart.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jfree.chart.util.ShapeList;
import org.junit.Test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.TestUtils;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.TextAnchor;

import javax.swing.event.EventListenerList;

/**
 * Tests for the {@link ShapeManager} class.
 */
public class ShapeManagerTest {

    @Test
    public void testEquals() {
        // have to use a concrete subclass...
        BarRenderer r1 = new BarRenderer();
        BarRenderer r2 = new BarRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));

        // shapeList
        r1.setSeriesShape(1, new Ellipse2D.Double(1, 2, 3, 4));
        assertFalse(r1.equals(r2));
        r2.setSeriesShape(1, new Ellipse2D.Double(1, 2, 3, 4));
        assertTrue(r1.equals(r2));

        // defaultShape
        r1.setDefaultShape(new Ellipse2D.Double(1, 2, 3, 4));
        assertFalse(r1.equals(r2));
        r2.setDefaultShape(new Ellipse2D.Double(1, 2, 3, 4));
        assertTrue(r1.equals(r2));

        // legendShape
        r1.setLegendShape(0, new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(r1.equals(r2));
        r2.setLegendShape(0, new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(r1.equals(r2));

        // baseLegendShape
        r1.setDefaultLegendShape(new Ellipse2D.Double(5.0, 6.0, 7.0, 8.0));
        assertFalse(r1.equals(r2));
        r2.setDefaultLegendShape(new Ellipse2D.Double(5.0, 6.0, 7.0, 8.0));
        assertTrue(r1.equals(r2));

    }

    /**
     * Confirm that cloning works.
     */
    @Test
    public void testCloning() throws CloneNotSupportedException {

        LineAndShapeRenderer r1 = new LineAndShapeRenderer();
        Rectangle2D shape = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        Rectangle2D baseShape = new Rectangle2D.Double(11.0, 12.0, 13.0, 14.0);
        r1.setDefaultShape(baseShape);
        r1.setDefaultLegendShape(new Rectangle(4, 3, 2, 1));

        LineAndShapeRenderer r2 = (LineAndShapeRenderer) r1.clone();
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));

        baseShape.setRect(4.0, 3.0, 2.0, 1.0);
        assertFalse(r1.equals(r2));
        r2.setDefaultShape(new Rectangle2D.Double(4.0, 3.0, 2.0, 1.0));
        assertTrue(r1.equals(r2));

        r1.setSeriesShape(0, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertFalse(r1.equals(r2));
        r2.setSeriesShape(0, new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0));
        assertTrue(r1.equals(r2));

        r1.setLegendShape(0, new Rectangle(9, 7, 3, 4));
        assertFalse(r1.equals(r2));
        r2.setLegendShape(0, new Rectangle(9, 7, 3, 4));
        assertTrue(r1.equals(r2));

        r1.setDefaultLegendShape(new Rectangle(3, 4, 1, 5));
        assertFalse(r1.equals(r2));
        r2.setDefaultLegendShape(new Rectangle(3, 4, 1, 5));
        assertTrue(r1.equals(r2));


    }

    /**
     * Tests each setter method to ensure that it sends an event notification.
     */
    @Test
    public void testEventNotification() {

        RendererChangeDetector detector = new RendererChangeDetector();
        BarRenderer r1 = new BarRenderer();  // have to use a subclass of
        // AbstractRenderer
        r1.addChangeListener(detector);

        // SHAPE
        detector.setNotified(false);
        r1.setSeriesShape(0, new Rectangle2D.Float());
        assertTrue(detector.getNotified());

        detector.setNotified(false);
        r1.setDefaultShape(new Rectangle2D.Float());
        assertTrue(detector.getNotified());

    }


    @Test
    public void generalShapeManagerFunctions() {
        BarRenderer r1 = new BarRenderer();

        r1.setSeriesShape(1, new Ellipse2D.Double(1, 2, 3, 4));
        assertTrue(r1.getSeriesShape(1).equals(new Ellipse2D.Double(1, 2, 3, 4)));
        assertFalse(r1.getSeriesShape(1).equals(new Ellipse2D.Double(4, 3, 2, 1)));

        r1.setDefaultShape(new Ellipse2D.Double(1, 2, 3, 4));
        assertTrue(r1.getDefaultShape().equals(new Ellipse2D.Double(1, 2, 3, 4)));

        r1.setAutoPopulateSeriesShape(true);
        assertTrue(r1.getAutoPopulateSeriesShape() == true);
        r1.setAutoPopulateSeriesShape(false);
        assertTrue(r1.getAutoPopulateSeriesShape() == false);

        r1.setSeriesShape(1, null);
        r1.setDefaultLegendShape(new Ellipse2D.Double(1, 2, 3, 4));
        assertTrue(r1.lookupLegendShape(1) == r1.defaultLegendShape);
        r1.setDefaultLegendShape(null);
        assertTrue(r1.lookupLegendShape(1).equals(new Ellipse2D.Double(1, 2, 3, 4)));

        r1.setDefaultShape(new Ellipse2D.Double(4, 3, 2, 1), false);
        assertTrue(r1.getDefaultShape().equals(new Ellipse2D.Double(4, 3, 2, 1)));




    }

    @Test
    public void defaultConstructorTest() {

        BarRenderer r1 = new BarRenderer();

        assertTrue(r1.shapeList.equals(new ShapeList()));
        assertTrue(r1.defaultShape.equals(ShapeManager.DEFAULT_SHAPE));
        assertTrue(r1.autoPopulateSeriesShape == true);
        assertTrue(r1.legendShapeList.equals(new ShapeList()));

//        assertTrue(r1.defaultLegendShape.equals(null));

    }


}
