package com.example.artbookkotlin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.artbookkotlin.databinding.RecyclerRowBinding

class RecyclerAdapter(val arrayList: ArrayList<Art>) : RecyclerView.Adapter<RecyclerAdapter.RecyclerHolder>() {

    class RecyclerHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RecyclerHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {

        holder.binding.recyclerRowTextView.text = arrayList.get(position).name

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,DetailsActivity::class.java)
            intent.putExtra("info",1)
            intent.putExtra("id",arrayList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }

    }
}