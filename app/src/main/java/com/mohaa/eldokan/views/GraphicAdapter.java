package com.mohaa.eldokan.views;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mohaa.eldokan.R;
import com.mohaa.eldokan.Utils.charts.Grid;
import com.mohaa.eldokan.Utils.charts.LineChartView;
import com.mohaa.eldokan.Utils.charts.PieChartView;
import com.mohaa.eldokan.models.GraphicItem;
import com.mohaa.eldokan.models.LineChartItem;
import com.mohaa.eldokan.models.PieChartItem;
import com.mohaa.eldokan.models.legend_item;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;

/**
 * Created by Thibaut on 18/01/16.
 *
 * La classe GraphicAdapter permet de dessiner un GraphicItem dans une listView
 *
 */
public class GraphicAdapter extends ArrayAdapter {

    public static final int TYPE_SEPARATOR = 0;
    public static final int TYPE_PIE = 1;
    public static final int TYPE_LINE = 2;

    private GraphicItem[] objects;
    private MyViewHolder viewHolder = null;
    private View MyConvertView;
    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return objects[position].getType();
    }

    public GraphicAdapter(Context context, int resource, GraphicItem[] objects) {
        super(context, resource, objects);
        this.objects = objects;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        GraphicItem listViewItem = objects[position];
        int listViewItemType = getItemViewType(position);
        MyConvertView = convertView;

        if (convertView == null) {

            if (listViewItemType == TYPE_SEPARATOR) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.separator, null);
                TextView textView = (TextView) convertView.findViewById(R.id.separator_text);
                viewHolder = new MyViewHolder(textView);
                viewHolder.getTextView().setText(listViewItem.getText());

            } else if (listViewItemType == TYPE_PIE) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.pie_chart_layout, null);

                PieChartView pie = (PieChartView) convertView.findViewById(R.id.pieChart);
                TextView total_text = (TextView) convertView.findViewById(R.id.text_total);
                ListView legend = (ListView) convertView.findViewById(R.id.pie_legend);
                viewHolder = new MyViewHolder(pie, total_text, legend);


                List<legend_item> list = new ArrayList<>();
                list = new ArrayList<>();

                ArrayList<PieChartItem> pie_items = listViewItem.getPie();
                int nb_item = pie_items.size();
                int total_events = listViewItem.getTotal_events();
                Log.i("Dashboard", "getView nombre d'items " + nb_item);
                for (int i = 0 ; i < nb_item ; i++){
                    viewHolder.getPiechartView().addValue(pie_items.get(i));
                    Double nb_events_item = (pie_items.get(i).getValue() * total_events / 100);
                    list.add(new legend_item(pie_items.get(i).getStrokeColor(), pie_items.get(i).getLegend(), nb_events_item.intValue()));
                }

                legendAdapter la = new legendAdapter(this.getContext(), list);
                viewHolder.getLegend().setAdapter(la);
                viewHolder.getTextView().setText("" + total_events);


            } else if (listViewItemType == TYPE_LINE) {

                convertView = LayoutInflater.from(getContext()).inflate(R.layout.line_chart_layout, null);

                LineChartView axis = (LineChartView) convertView.findViewById(R.id.LineChart);

                RelativeLayout lineChart_layout = (RelativeLayout) convertView.findViewById(R.id.lineChart_layout);
                LinearLayout line_button_layout = (LinearLayout) convertView.findViewById(R.id.line_chart_button_layout);

                TextView grid_button = (TextView) convertView.findViewById(R.id.grid_button);

                ArrayList<LineChartView> lines = new ArrayList<>();
                ArrayList<TextView> line_buttons = new ArrayList<>();

                lines.add(axis);

                viewHolder = new MyViewHolder(lines, lineChart_layout, line_button_layout, line_buttons);
                viewHolder.addButton(grid_button);

                List<legend_item> list = new ArrayList<>();

                ArrayList<LineChartItem> line_items = listViewItem.getLine_items();
                int nb_item = line_items.size();

                Log.i("Dashboard", "getView nombre d'items " + nb_item);

                // on commence par dessiner les axes sur le premier LineChartView
                for (int i = 0 ; i < nb_item ; i++){
                    viewHolder.getLinechart().get(0).addValue(line_items.get(i), LineChartView.MODE_AXIS);
                }
                Grid grid = viewHolder.getLinechart().get(0).getGrid();

                // puis on prépare la grille

                // Donc on crée un nouveau LineChartView
                viewHolder.getLinechart().add(new LineChartView(convertView.getContext(), null));
                viewHolder.getLines_layout().addView(viewHolder.getLinechart().get(1), viewHolder.getLinechart().get(0).getLayoutParams());

                // puis on trace la grille

                for (int i = 0 ; i < nb_item ; i++){
                    viewHolder.getLinechart().get(1).addValue(line_items.get(i), LineChartView.MODE_DRAW_GRID, grid);
                }


                // puis on dessine chaque lineChart dans une view différente
                for(int i = 0 ; i < nb_item ; i++){

                    // on crée un nouveau LineChartView
                    viewHolder.getLinechart().add(new LineChartView(convertView.getContext(), null));
                    viewHolder.getLines_layout().addView(viewHolder.getLinechart().get(i + 2), viewHolder.getLinechart().get(0).getLayoutParams());

                    // et le bouton qui lui est associé
                    final TextView button = new TextView(convertView.getContext());
                    button.setBackground(convertView.getResources().getDrawable(R.drawable.line_chart_button_selected));
                    button.setText(line_items.get(i).getLabel());
                    button.setTextColor(line_items.get(i).getColor());
                    button.setTag(i);

                    viewHolder.addButton(button);

                    LinearLayout.LayoutParams lp = new  LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,  LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMarginStart(5);

                    viewHolder.getLine_button_layout().addView(viewHolder.getLine_buttons().get(i + 1), lp);


                    viewHolder.getLinechart().get(i+2).addValue(line_items.get(i), LineChartView.MODE_DRAW_LINES, grid);
                }
            }

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (MyViewHolder) convertView.getTag();
        }


        return convertView;
    }

}
class MyViewHolder {
    TextView text;
    PieChartView piechart;
    ArrayList<LineChartView> linechart;
    RelativeLayout lines_layout;
    LinearLayout line_button_layout;
    ArrayList<TextView> line_buttons;
    ListView legend;

    // viewHolder pour les séparateurs
    public MyViewHolder(TextView text) {
        this.text = text;
    }

    // viewHolder pour les PieCharts
    public MyViewHolder(PieChartView piechart, TextView text, ListView legend) {
        this.piechart = piechart;
        this.text = text;
        this.legend = legend;
    }

    public ArrayList<TextView> getLine_buttons() {
        return line_buttons;
    }

    public void setLine_buttons(ArrayList<TextView> line_buttons) {
        this.line_buttons = line_buttons;
    }

    public LinearLayout getLine_button_layout() {

        return line_button_layout;
    }

    public void setLine_button_layout(LinearLayout line_button_layout) {
        this.line_button_layout = line_button_layout;
    }

    // viewHolder pour les LineCharts
    public MyViewHolder(ArrayList<LineChartView> linechart, RelativeLayout lines_layout, LinearLayout line_button_layout, ArrayList<TextView> line_buttons) {
        this.linechart = linechart;
        this.lines_layout = lines_layout;
        this.line_button_layout = line_button_layout;
        this.line_buttons = line_buttons;

    }

    public TextView getTextView() {
        return text;
    }

    public PieChartView getPiechartView() {
        return piechart;
    }

    public void setText(TextView text) {
        this.text = text;
    }

    public void setPiechart(ArrayList<PieChartItem> list) {
        //this.text = text;

    }

    public void addValue(PieChartItem item){
        this.piechart.addValue(item);
    }

    public void invalidate(){
        this.piechart.invalidate();
    }

    /*public void addValue(PieChartItem item){
        this.piechart.addValue(item);
    }*/
    public ListView getLegend(){
        return this.legend;
    }
    public void setLegend(ListView legend){
        this.legend = legend;
    }

    public ArrayList<LineChartView> getLinechart() {
        return linechart;
    }

    public void setLinechart(ArrayList<LineChartView> linechart) {
        this.linechart = linechart;
    }

    public RelativeLayout getLines_layout() {
        return lines_layout;
    }

    public void setLines_layout(RelativeLayout lines_layout) {
        this.lines_layout = lines_layout;
    }

    public void addButton(TextView button){
        button.setOnClickListener(mMyClickListener);
        this.line_buttons.add(button);
    }

    private View.OnClickListener mMyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Log.d("Dashboard", "button " + v.getText() + " clicked ID = " + v.getTag());

            int ID = 0;
            if(v.getId() == R.id.grid_button) {
                ID = 1;
                //Log.i("Dashboard", "onClick GRID on/off");
            } else {
                ID = (int)v.getTag() + 2;
            }
            //Log.d("Dashboard", "nb lineChart " + linechart.size());

            if(linechart.get(ID).getVisibility() == View.VISIBLE){
                linechart.get(ID).setVisibility(View.INVISIBLE);
                v.setBackground(v.getResources().getDrawable(R.drawable.line_chart_button));
            } else {
                linechart.get(ID).setVisibility(View.VISIBLE);
                v.setBackground(v.getResources().getDrawable(R.drawable.line_chart_button_selected));
            }


        }
    };
}