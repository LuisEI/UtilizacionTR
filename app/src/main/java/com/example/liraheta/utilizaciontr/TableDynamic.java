package com.example.liraheta.utilizaciontr;

import android.content.Context;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class TableDynamic {

    private TableLayout tableLayout;
    private Context context;
    private String[] header;
    private ArrayList<String[]> data;
    private TableRow tableRow;
    private TextView txtCell;
    private int indexC;
    private int indexR;
    private boolean multiColor = false;
    private int firtColor, secondColor, textColor;
    private int lineColor = -16777216;

    public TableDynamic(TableLayout tableLayout, Context context) {
        this.tableLayout = tableLayout;
        this.context = context;
    }

    public void addHeader(String[] header){
        this.header = header;
        createHeader();
    }

    public void addData(ArrayList<String[]> data){
        this.data = data;
        createDataTable();
    }

    private void newRow(){
        tableRow = new TableRow(context);
    }

    private void newCell(){
        txtCell = new TextView(context);
        txtCell.setGravity(Gravity.CENTER);
        txtCell.setTextSize(14);
        txtCell.setHeight(40);
        txtCell.setMinWidth(250);
    }

    private void createHeader(){
        indexC = 0;
        newRow();
        while (indexC < header.length){
            newCell();
            txtCell.setText(header[indexC++]);
            tableRow.addView(txtCell, newTableRowParams());
        }
        tableLayout.addView(tableRow);
    }

    private void createDataTable(){
        String info;
        for (indexR = 1; indexR <= data.size(); indexR++){ //Editado header.length por data.size()
            newRow();
            for (indexC = 0; indexC < header.length; indexC++){
                newCell();
                String[] row = data.get(indexR-1);
                info = (indexC < row.length)? row[indexC] : "";
                txtCell.setText(info);
                tableRow.addView(txtCell, newTableRowParams());
            }
            tableLayout.addView(tableRow);
        }
    }

    public void Clear(){
        tableLayout.removeAllViews();
    }

    public void addItem(String[] item){
        String info;
        data.add(item);
        indexC = 0;
        newRow();
        while (indexC < header.length){
            newCell();
            info = (indexC < item.length)? item[indexC++] : "";
            txtCell.setText(info);
            tableRow.addView(txtCell, newTableRowParams());
        }
        tableLayout.addView(tableRow, data.size()); //Se quito el -1
        reColoring();
        lineColor(lineColor);
    }

    public void backgroundHeader(int color){
        indexC = 0;
        while (indexC < header.length){
            txtCell = getCell(0, indexC++);
            txtCell.setBackgroundResource(color);
        }
    }

    public void backgroundData(int firtColor, int secondColor){
        for (indexR = 1; indexR <= data.size(); indexR++){ //Se cambia header.length por data.size();
            multiColor = !multiColor;
            for (indexC = 0; indexC < header.length; indexC++){
                txtCell = getCell(indexR, indexC);
                txtCell.setBackgroundColor((multiColor)? firtColor : secondColor);
            }
        }
        this.firtColor = firtColor;
        this.secondColor = secondColor;
    }

    public void lineColor(int color){
        indexR = 0;
        while (indexR <= data.size()){ //Se agrega el igual
            getRow(indexR++).setBackgroundColor(color);
        }
        this.lineColor = color;
    }

    public void textColorData(int color){
        for (indexR = 1; indexR <= data.size(); indexR++){ // Se cambia el header.length por data.size()
            for (indexC = 0; indexC < header.length; indexC++){
                getCell(indexR, indexC).setTextColor(color);
            }
        }
        this.textColor = color;
    }

    public void textColorHeader(int color){
        indexC = 0;
        while (indexC < header.length){
            getCell(0, indexC++).setTextColor(color);
        }
    }

    public void reColoring(){
        indexC = 0;
        multiColor = !multiColor;
        while (indexC < header.length){
            txtCell = getCell(data.size(), indexC++); //Se quita el -1
            txtCell.setBackgroundColor((multiColor)? firtColor : secondColor);
            txtCell.setTextColor(textColor);
        }
    }

    private TableRow getRow(int index){
        return (TableRow) tableLayout.getChildAt(index);
    }

    private TextView getCell(int rowIndex, int columIndex){
        tableRow = getRow(rowIndex);
        return (TextView) tableRow.getChildAt(columIndex);
    }

    private TableRow.LayoutParams newTableRowParams(){
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.setMargins(1,1,1,1);
        params.weight = 1;
        return params;
    }

}
