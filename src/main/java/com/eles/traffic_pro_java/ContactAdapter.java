package com.eles.traffic_pro_java;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private static List<Contact> contactList;

    public ContactAdapter(List<Contact> contactList) {
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.name.setText(contact.getName());
        holder.phone.setText(contact.getPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone;
        ImageView deleteButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contact_name);
            phone = itemView.findViewById(R.id.contact_phone);
            deleteButton=itemView.findViewById(R.id.delete_contact);

            deleteButton.setOnClickListener(v -> {
                int position=getAdapterPosition();
                if(position!=RecyclerView.NO_POSITION){
                    removeContact(position);
                }
            });
        }

        private void removeContact(int position) {
            if (position >= 0 && position < contactList.size()) {
                contactList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, contactList.size()); // Update indices
            }
        }
    }
}
