package com.jacob.novelview.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jacob.novelview.DTO.LoadDTO
import com.jacob.novelview.FileLoad
import com.jacob.novelview.R
import kotlinx.android.synthetic.main.file_list_item.view.*
import kotlinx.android.synthetic.main.file_list_item.view.iv_type
import kotlinx.android.synthetic.main.file_list_item.view.tv_folder_nm
import kotlinx.android.synthetic.main.file_list_item2.view.*


class LoadAdapter(var list:List<LoadDTO>, val clickListener: FileLoad, var type:String) : RecyclerView.Adapter<LoadAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var folderNm = itemView.tv_folder_nm
        var image = itemView.iv_type
        var more = itemView.btn_more

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        var itemLayout : Int
        if(type == "external"){
            itemLayout = R.layout.file_list_item
        } else {
            itemLayout = R.layout.file_list_item2
        }
        val itemView =
            LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        return CustomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        //onCreateViewHolder에서 넘어온다.
        // item틀과 list 내용을 합쳐주는 역할을 한다.
        val load = list[position]
        holder.image.setImageResource(load.type)
        holder.folderNm.text = load.name
        //클릭 이벤트를 준다.

        holder.itemView.setOnClickListener {
            clickListener.onItemClick(load)
        }
        holder.more.setOnClickListener {
            clickListener.delItemClick(load)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface ClickListener {
        fun onItemClick(loadDTO: LoadDTO)
        fun delItemClick(loadDTO: LoadDTO)
    }
}