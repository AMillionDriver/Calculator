package com.axoloth.calculator.by.sky.logic

import android.content.Context
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.NestedScrollView
import com.axoloth.calculator.by.sky.MainActivity
import com.axoloth.calculator.by.sky.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Logika Konversi Mata Uang dengan Smart Caching (1 Jam) dan Offline Support.
 */
fun setupKursLogic(activity: AppCompatActivity, view: View) {
    val etValueFrom: EditText = view.findViewById(R.id.etValueFrom)
    val tvValueTo: TextView = view.findViewById(R.id.tvValueTo)
    val tvUnitFrom: TextView = view.findViewById(R.id.tvUnitFrom)
    val tvUnitTo: TextView = view.findViewById(R.id.tvUnitTo)
    val tvLastUpdated: TextView = view.findViewById(R.id.tvLastUpdated)
    val nestedScroll: NestedScrollView? = view.findViewById(R.id.nestedScrollKurs)

    val pref = activity.getSharedPreferences("kurs_cache", Context.MODE_PRIVATE)
    val gson = Gson()

    // Matikan keyboard sistem
    etValueFrom.showSoftInputOnFocus = false
    etValueFrom.requestFocus()

    // State
    var fromCode = "USD"
    var toCode = "IDR"
    var ratesMap: Map<String, Double>? = null

    // Inisialisasi Teks Unit
    tvUnitFrom.text = fromCode
    tvUnitTo.text = toCode

    // Data Mata Uang (Code to Full Name)
    val currencyData = mapOf(
        "USD" to "US Dollar", "IDR" to "Indonesian Rupiah", "EUR" to "Euro",
        "GBP" to "British Pound", "JPY" to "Japanese Yen", "SGD" to "Singapore Dollar",
        "MYR" to "Malaysian Ringgit", "AUD" to "Australian Dollar", "CNY" to "Chinese Yuan",
        "KRW" to "South Korean Won", "THB" to "Thai Baht", "VND" to "Vietnamese Dong",
        "INR" to "Indian Rupee", "SAR" to "Saudi Riyal", "AED" to "UAE Dirham"
    )

    // Ambil API_KEY dari MainActivity (Remote Config)
    val apiKey = if (activity is MainActivity) activity.getCurrencyApiKey() else ""

    fun calculateConversion() {
        val inputStr = etValueFrom.text.toString().replace(",", ".")
        if (inputStr.isEmpty() || ratesMap == null) {
            tvValueTo.text = "0"
            return
        }

        try {
            val inputNum = inputStr.toDouble()
            val rate = ratesMap!![toCode] ?: 1.0
            val result = inputNum * rate
            tvValueTo.text = String.format(Locale.US, "%,.2f", result)
        } catch (e: Exception) {
            tvValueTo.text = "0"
        }
    }

    fun saveCache(base: String, rates: Map<String, Double>) {
        val json = gson.toJson(rates)
        pref.edit().apply {
            putString("rates_$base", json)
            putLong("time_$base", System.currentTimeMillis())
            apply()
        }
    }

    fun loadCache(base: String): Boolean {
        val json = pref.getString("rates_$base", null)
        val lastTime = pref.getLong("time_$base", 0)
        val currentTime = System.currentTimeMillis()

        if (json != null) {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            ratesMap = gson.fromJson(json, type)
            
            val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(lastTime))
            tvLastUpdated.text = "Data: $dateStr"
            
            calculateConversion()
            if (currentTime - lastTime < 3600000) return true
        }
        return false
    }

    fun fetchRates() {
        if (loadCache(fromCode)) return 

        if (apiKey.isEmpty()) return

        KursApiService.create().getLatestRates(apiKey, fromCode).enqueue(object : Callback<ExchangeRateResponse> {
            override fun onResponse(call: Call<ExchangeRateResponse>, response: Response<ExchangeRateResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    ratesMap = response.body()!!.conversionRates
                    saveCache(fromCode, ratesMap!!)
                    
                    val dateStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    tvLastUpdated.text = "Updated: $dateStr"
                    calculateConversion()
                }
            }
            override fun onFailure(call: Call<ExchangeRateResponse>, t: Throwable) {
                if (ratesMap == null) Toast.makeText(activity, "Offline", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // --- Cursor Input ---
    fun insertText(text: String) {
        val scrollY = nestedScroll?.scrollY ?: 0
        val start = etValueFrom.selectionStart
        val end = etValueFrom.selectionEnd
        etValueFrom.text.replace(start, end, text)
        calculateConversion()
        
        nestedScroll?.post {
            nestedScroll.scrollTo(0, scrollY)
        }
    }

    // --- Bottom Sheet Picker ---
    fun showCurrencyPicker(isFrom: Boolean) {
        val dialog = BottomSheetDialog(activity)
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.layout_currency_picker, null)
        dialog.setContentView(dialogView)

        val rv = dialogView.findViewById<RecyclerView>(R.id.rvCurrencies)
        val searchView = dialogView.findViewById<SearchView>(R.id.searchView)
        
        val fullList = currencyData.toList()
        var filteredList = fullList

        class CurrencyAdapter(private var items: List<Pair<String, String>>) : RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {
            inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
                val code: TextView = v.findViewById(R.id.tvCode)
                val name: TextView = v.findViewById(R.id.tvFullName)
            }
            override fun onCreateViewHolder(p: ViewGroup, t: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_currency, p, false))
            override fun onBindViewHolder(h: ViewHolder, p: Int) {
                val item = items[p]
                h.code.text = item.first
                h.name.text = item.second
                h.itemView.setOnClickListener {
                    if (isFrom) {
                        fromCode = item.first
                        tvUnitFrom.text = fromCode
                        fetchRates()
                    } else {
                        toCode = item.first
                        tvUnitTo.text = toCode
                        calculateConversion()
                    }
                    dialog.dismiss()
                }
            }
            override fun getItemCount() = items.size
            fun filter(list: List<Pair<String, String>>) { items = list; notifyDataSetChanged() }
        }

        val adapter = CurrencyAdapter(filteredList)
        rv.layoutManager = LinearLayoutManager(activity)
        rv.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(q: String?): Boolean {
                filteredList = if (q.isNullOrBlank()) fullList 
                else fullList.filter { it.first.contains(q, true) || it.second.contains(q, true) }
                adapter.filter(filteredList)
                return true
            }
        })

        dialog.show()
    }

    tvUnitFrom.setOnClickListener { showCurrencyPicker(true) }
    tvUnitTo.setOnClickListener { showCurrencyPicker(false) }

    // --- Keypad ---
    val numericButtons = mapOf(
        R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2", R.id.btn3 to "3",
        R.id.btn4 to "4", R.id.btn5 to "5", R.id.btn6 to "6", R.id.btn7 to "7",
        R.id.btn8 to "8", R.id.btn9 to "9"
    )

    numericButtons.forEach { (id, value) ->
        view.findViewById<Button>(id).setOnClickListener { 
            playAnim(activity, it)
            insertText(value) 
        }
    }

    view.findViewById<Button>(R.id.btnKoma).setOnClickListener { 
        playAnim(activity, it)
        if (!etValueFrom.text.contains(",")) insertText(",") 
    }
    
    view.findViewById<Button>(R.id.btnDel).setOnClickListener {
        playAnim(activity, it)
        val start = etValueFrom.selectionStart
        val end = etValueFrom.selectionEnd
        if (start > 0 || start != end) {
            if (start == end) {
                etValueFrom.text.delete(start - 1, start)
            } else {
                etValueFrom.text.delete(start, end)
            }
        }
        calculateConversion()
    }

    view.findViewById<Button>(R.id.btnBack).setOnClickListener {
        playAnim(activity, it)
        activity.onBackPressedDispatcher.onBackPressed()
    }

    fetchRates()
}

private fun playAnim(activity: AppCompatActivity, view: View) {
    val anim = AnimationUtils.loadAnimation(activity, R.anim.button_click)
    view.startAnimation(anim)
}
