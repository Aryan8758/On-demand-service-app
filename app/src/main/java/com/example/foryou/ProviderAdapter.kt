package com.example.foryou

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout

class ProviderAdapter(private var providers: List<ProviderModelClass>, private val isHorizontal: Boolean = false):  // default = grid) :
    RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder>() {


    class ProviderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.nameTextView)
        val serviceText: TextView = itemView.findViewById(R.id.serviceTextView)
        val priceText: TextView = itemView.findViewById(R.id.priceTextView)
       val imageView: ImageView = itemView.findViewById(R.id.providerImage)
       val linear_Layout: CardView = itemView.findViewById(R.id.linear)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_provider, parent, false)
        return ProviderViewHolder(view)
    }

//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        TODO("Not yet implemented")
//    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val provider = providers[position]
        val context=holder.itemView.context
        holder.nameText.text = provider.name
        holder.serviceText.text = provider.service
        holder.priceText.text = provider.price
        //holder.linear_Layout.setBackgroundResource(provider.bg)
        holder.itemView.setOnClickListener {
            val intent=Intent(context,Provider_Detail::class.java)
            intent.putExtra("providername",provider.name)
            intent.putExtra("servicename",provider.service)
            intent.putExtra("image",provider.image)
            intent.putExtra("ProviderId",provider.providerId)
        //    intent.putExtra("providername",provider.name)
            context.startActivity(intent)
        }

        if(provider.image!=null){
            val img = decodeBase64ToBitmap(provider.image)
            holder.imageView.setImageBitmap(img)
        }else{
            holder.imageView.setImageResource(R.drawable.tioger)
        }
        // ✅ Set layout width based on layout type
        val layoutParams = holder.linear_Layout.layoutParams
        if (isHorizontal) {
            layoutParams.width = dpToPx(context, 200)
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        holder.linear_Layout.layoutParams = layoutParams
    }

    override fun getItemCount(): Int = providers.size
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

}
