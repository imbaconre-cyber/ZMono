package app.zmono.font;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.VH> {
    public interface OnFontClick { void onClick(FontItem font, int position); }

    private final List<FontItem> items = new ArrayList<>();
    private final OnFontClick listener;
    private int selected = -1;

    public FontAdapter(OnFontClick listener) {
        this.listener = listener;
    }

    public void setItems(List<FontItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        selected = -1;
        notifyDataSetChanged();
    }

    public void setSelected(int pos) {
        int old = selected;
        selected = pos;
        if (old >= 0) notifyItemChanged(old);
        if (pos >= 0) notifyItemChanged(pos);
    }

    public FontItem getSelected() {
        if (selected >= 0 && selected < items.size()) return items.get(selected);
        return null;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FontItem f = items.get(position);
        h.title.setText(f.name);
        h.subtitle.setText(f.size + "  •  " + f.category);
        h.itemView.setSelected(position == selected);
        h.itemView.setActivated(position == selected);
        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            setSelected(pos);
            if (listener != null) listener.onClick(items.get(pos), pos);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            subtitle = itemView.findViewById(android.R.id.text2);
        }
    }
}