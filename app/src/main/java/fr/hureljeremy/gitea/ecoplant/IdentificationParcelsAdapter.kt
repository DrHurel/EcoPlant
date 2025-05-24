package fr.hureljeremy.gitea.ecoplant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IdentificationParcelsAdapter(private val items: List<IdentificationParcelItem>) :
    RecyclerView.Adapter<IdentificationParcelsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.identification_parcels_item, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.identification_text)
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.hint = "${item.label}: ${item.value}"
    }

    override fun getItemCount() = items.size
}