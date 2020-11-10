package com.polant.projectsport.adapter.food;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.polant.projectsport.R;
import com.polant.projectsport.data.database.Database;


public class AdapterCategoryFood extends CursorAdapter {

    private static final int LAYOUT = R.layout.list_adapter_category_food;

    private LayoutInflater layoutInflater;
    private Context context;

    public AdapterCategoryFood(Context _context, Cursor c, int flags) {
        super(_context, c, flags);
        context = _context;
        layoutInflater = LayoutInflater.from(_context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(LAYOUT, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String category = cursor.getString(cursor.getColumnIndex(Database.FOOD_CATEGORY));
        TextView categoryTextView = (TextView) view.findViewById(R.id.textViewCategoryFood);

        categoryTextView.setText(category);
    }
}
