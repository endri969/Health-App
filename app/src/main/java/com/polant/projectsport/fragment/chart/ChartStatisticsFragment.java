package com.polant.projectsport.fragment.chart;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.polant.projectsport.activity.ArticlesActivity;
import com.polant.projectsport.R;
import com.polant.projectsport.data.database.Database;
import com.polant.projectsport.model.StatisticsDay;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;

/**
 * Created by Антон on 25.10.2015.
 */
public class ChartStatisticsFragment extends Fragment {

    private static final int LAYOUT = R.layout.fragment_statistics_chart;


    public static final String STATISTICS_INTERVAL = "STATISTICS_INTERVAL";
    public static final int STATISTICS_WEEK = 7;
    public static final int STATISTICS_MONTH = 30;

    private View view;
    private Database DB;
    private int interval;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        return view;
    }

    //Метод-фабрика.
    public static ChartStatisticsFragment getInstance(int intervalValue){
        Bundle args = new Bundle();
        args.putInt(STATISTICS_INTERVAL, intervalValue);

        ChartStatisticsFragment fragment = new ChartStatisticsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null){
            interval = getArguments().getInt(STATISTICS_INTERVAL);
        }

        DB = ((ArticlesActivity) getActivity()).getDatabase();

        initChart();
    }

    //Инициализирую график.
    private void initChart() {
        //Числовые данные графика.
        XYMultipleSeriesDataset dataset = initDataSet();
        //Визуализация.
        ArrayList<XYSeriesRenderer> renderers = initSeriesRenderers();
        //Дополнительная настройка визуализации.
        XYMultipleSeriesRenderer mRenderer = initMultipleSeriesRenderer(renderers);
        //Здесь инициализирую сам контейнер-лайаут, который бужет содержать график.
        initChartLayout(dataset, mRenderer);
    }

    private XYMultipleSeriesDataset initDataSet(){
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        XYSeries series = new XYSeries(getString(R.string.text_your_statistics));
        ArrayList<StatisticsDay> list = DB.getStatistics(interval);

        for (int i = 0; i < list.size(); i++) {
            series.add(i, list.get(i).getDelta());
        }
        dataset.addSeries(series);

        XYSeries normalLineSeries= new XYSeries(getString(R.string.text_your_normal_ccal));
        int normalCaloriesValue =  DB.getUserParametersInfo().normalCaloriesCount(getActivity());
        for (int i = 0; i < list.size(); i++) {
            normalLineSeries.add(i, normalCaloriesValue);
        }
        dataset.addSeries(normalLineSeries);
        return  dataset;
    }

    //Визуализация.
    private ArrayList<XYSeriesRenderer> initSeriesRenderers(){
        ArrayList<XYSeriesRenderer> list = new ArrayList<>();
        list.add(initSeriesRender("data"));
        list.add(initSeriesRender("normal_line"));
        return list;
    }


    private XYSeriesRenderer initSeriesRender(String type) {
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setLineWidth(4);
        if (type.equals("data")){
            if (interval == STATISTICS_WEEK)
                renderer.setColor(Color.RED);
            else
                renderer.setColor(Color.BLUE);
        }
        else{
            renderer.setColor(Color.GREEN);
        }
        renderer.setDisplayBoundingPoints(true);
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setPointStrokeWidth(6);
        return renderer;
    }



    private XYMultipleSeriesRenderer initMultipleSeriesRenderer(ArrayList<XYSeriesRenderer> renderers) {
        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        for (XYSeriesRenderer renderer : renderers){
            mRenderer.addSeriesRenderer(renderer);
        }


        mRenderer.setMarginsColor(getResources().getColor(R.color.mainBackground));
        mRenderer.setPanEnabled(true, true);
        mRenderer.setZoomInLimitX(2);
        mRenderer.setZoomInLimitY(2);


        mRenderer.setShowGrid(true);
        mRenderer.setGridColor(Color.BLACK);

        mRenderer.setLabelsTextSize(35);
        mRenderer.setXLabelsColor(Color.BLACK);
        mRenderer.setYLabelsColor(0, Color.BLACK);

        mRenderer.setAxesColor(Color.BLACK);
        mRenderer.setYTitle(getString(R.string.text_ccal));
        mRenderer.setXTitle(getString(R.string.text_days));
        mRenderer.setYLabelsAlign(Paint.Align.LEFT);
        mRenderer.setAxisTitleTextSize(40);

        mRenderer.setShowLegend(false);
        //Отступы всего графика.
        mRenderer.setMargins(new int[]{50, 50, 50, 50});

        mRenderer.setBarSpacing(0.5);

        return mRenderer;
    }

    private void initChartLayout(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer mRenderer) {

        LinearLayout chartLayout = (LinearLayout) view.findViewById(R.id.chart);


//        GraphicalView chartView;
//        if (interval == STATISTICS_WEEK) {

//            chartView = ChartFactory.getLineChartView(getActivity(), dataset, mRenderer);
//        }
//        else{

//            chartView = ChartFactory.getBarChartView(getActivity(), dataset, mRenderer, BarChart.Type.DEFAULT);
//        }


        GraphicalView chartView;
        chartView = ChartFactory.getLineChartView(getActivity(), dataset, mRenderer);

        chartLayout.addView(chartView, 0);
    }
}
