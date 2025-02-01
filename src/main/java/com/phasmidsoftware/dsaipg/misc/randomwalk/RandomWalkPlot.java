package com.phasmidsoftware.dsaipg.misc.randomwalk;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class RandomWalkPlot {
    public static void main(String[] args) {
        XYSeries randomWalkSeries = new XYSeries("Random Walk Distance");
        XYSeries sqrtMSeries = new XYSeries("√m");
        XYSeries sqrtPiOver4MSeries = new XYSeries("√(π/4 * m)");
        XYSeries sqrt2OverPiMSeries = new XYSeries("√(2/π * m)");

        int[] ms = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 30, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000};

        for (int m : ms) {
            randomWalkSeries.add(m, RandomWalk.randomWalkMulti(m, 10000));
            sqrtMSeries.add(m, Math.sqrt(m));
            sqrt2OverPiMSeries.add(m, Math.sqrt((2/Math.PI) * m));
            sqrtPiOver4MSeries.add(m, Math.sqrt((Math.PI/4) * m));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(randomWalkSeries);
        dataset.addSeries(sqrtMSeries);
        dataset.addSeries(sqrtPiOver4MSeries);
        dataset.addSeries(sqrt2OverPiMSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "2D Random Walk: Distance vs Steps",
                "Steps (m)",
                "Distance",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        JFrame frame = new JFrame("Random Walk Plot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);
    }
}
