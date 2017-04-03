package com.example.bareitan.movierent;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by bareitan on 01/04/2017.
 */

public class CategoriesAdapter extends
        RecyclerView.Adapter<CategoriesAdapter.ViewHolder>
{
    private List<Category> mCategories;
    private Context mContext;

    public CategoriesAdapter(Context context, List<Category> categories) {
        mCategories = categories;
        mContext = context;
    }
    public Context getContext() {
        return mContext;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View categoryView = inflater.inflate(R.layout.item_category,parent,false);

        ViewHolder viewHolder = new ViewHolder(categoryView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Category category = mCategories.get(position);

        final TextView categroyNameTV = holder.categoryNameTV;
        categroyNameTV.setText(category.getName());

        Button deleteButton = holder.deleteCategoryButton;
        Button editButton = holder.editCategoryButton;
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, categroyNameTV.getText() + " was removed.", Toast.LENGTH_SHORT).show();
                mCategories.remove(position);
                notifyDataSetChanged();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                LayoutInflater inflater = LayoutInflater.from(mContext);
                final View dialogView = inflater.inflate(R.layout.dialog_edit_cateogry, null);
                dialogBuilder.setView(dialogView);

                final EditText edt = (EditText) dialogView.findViewById(R.id.category_name);
                edt.setText(mCategories.get(position).getName());
                dialogBuilder.setMessage("Enter a new category name");
                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(mContext, "New Category name: " + edt.getText().toString(), Toast.LENGTH_SHORT).show();
                        mCategories.get(position).setName(edt.getText().toString());
                        notifyDataSetChanged();
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                AlertDialog b = dialogBuilder.create();
                b.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {

        public TextView categoryNameTV;
        public Button deleteCategoryButton;
        public Button editCategoryButton;

        public ViewHolder(View itemView) {
            super(itemView);

            categoryNameTV = (TextView) itemView.findViewById(R.id.category_name);
            deleteCategoryButton = (Button) itemView.findViewById(R.id.delete_button);
            editCategoryButton = (Button) itemView.findViewById(R.id.edit_button);
        }

    }
}
