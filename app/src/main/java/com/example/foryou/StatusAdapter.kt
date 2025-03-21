import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foryou.R
import com.example.foryou.StatusModelClass

class StatusAdapter(
    private val bookings: List<StatusModelClass>,
    private val onWorkComplete: (StatusModelClass) -> Unit
) : RecyclerView.Adapter<StatusAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.customerName)
        val bookingDate: TextView = itemView.findViewById(R.id.bookingDate)
        val statusText: TextView = itemView.findViewById(R.id.statusChip)
        val workCompleteBtn: Button = itemView.findViewById(R.id.workCompleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.status_adapter_xml, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = bookings[position]

        holder.customerName.text = "Customer: ${booking.customerName}"
        holder.bookingDate.text = "${booking.bookingDate},${booking.timeSlot}"
        holder.statusText.text = booking.status

        // Status wise Button Visibility
        when (booking.status) {
            "Accepted" -> {
                holder.statusText.setBackgroundColor(Color.parseColor("#008000"))
                holder.workCompleteBtn.visibility = View.VISIBLE
                holder.workCompleteBtn.setOnClickListener { onWorkComplete(booking) }
            }
            "Rejected" -> {
                holder.statusText.setBackgroundColor(Color.parseColor("#FF0000"))
                holder.workCompleteBtn.visibility = View.GONE
            }
            "Order Cancel" -> {
                holder.statusText.setBackgroundColor(Color.parseColor("#FF0000"))
                holder.workCompleteBtn.visibility = View.GONE
            }
            "Completed" -> {
            holder.statusText.setBackgroundColor(Color.parseColor("#008000"))
            holder.workCompleteBtn.visibility = View.GONE
        }
            else -> {
                holder.workCompleteBtn.visibility = View.GONE
            }
        }
    }


    override fun getItemCount(): Int = bookings.size
}
