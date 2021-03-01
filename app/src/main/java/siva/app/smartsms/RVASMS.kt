package siva.app.smartsms

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import siva.app.smartsms.databinding.RvSmsBinding
import siva.app.smartsms.databinding.RvSmsHeaderBinding


class RVASMS(val smsList:List<SMS_Item>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class RVHSMS(binding: RvSmsBinding):RecyclerView.ViewHolder(binding.root)
    class RVHSMSHeader(binding: RvSmsHeaderBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            0,2 -> RVHSMS(RvSmsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> RVHSMSHeader(RvSmsHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RVHSMSHeader){
            val headerBinding = RvSmsHeaderBinding.bind(holder.itemView)
            headerBinding.tvDate.text = smsList[position].dateTIme
        }else{
            val smsBinding = RvSmsBinding.bind(holder.itemView)
            smsBinding.tvMessage.text = smsList[position].sms
            smsBinding.tvPhone.text = smsList[position].from
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (smsList[position].type == 0 && position ==1){
            2
        }else{
            smsList[position].type
        }
    }

    override fun getItemCount(): Int {
        return smsList.size
    }
}