package com.example.restaurant.presentration.ShopSearch

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.restaurant.R
import com.example.restaurant.data.entities.Shop
import com.example.restaurant.databinding.ShopSearchFragmentBinding
import com.example.restaurant.presentration.searchHistory.SearchHistoryActivity
import com.example.restaurant.usecase.Constant
import com.example.restaurant.usecase.Resource
import com.example.restaurant.usecase.autoCleared
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopsearchFragment : Fragment(), ShopsAdapter.ShopItemListener {

    private var binding: ShopSearchFragmentBinding  by autoCleared()
    private val viewModel: ShopsearchViewModel by viewModels()
    private lateinit var adapter: ShopsAdapter
    private  var list = ArrayList<Shop>()
    private var searchHistoryList = ArrayList<String?>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ShopSearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        Log.e("position value get ", prefs.getString("data","")!!)
        if(!prefs.getString("data","").equals("")){
            adapter.filter.filter(prefs.getString("data",""))
            searchHistoryList.add(prefs.getString("data",""))
            saveArrayList(searchHistoryList,"search_history_list")
        }else{

            binding.searchNameEt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    adapter.filter.filter(s)
                    Handler().postDelayed(Runnable
                    {
                        searchHistoryList.add(s.toString())
                        saveArrayList(searchHistoryList,"search_history_list")
                    }, 2 * 1000
                    )
                }
            })
        }

        binding.searchHistory.setOnClickListener {
            startActivity(Intent(requireContext(),SearchHistoryActivity::class.java))
        }
        getView()?.setFocusableInTouchMode(true)
        getView()?.requestFocus()
        getView()?.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                return if (keyCode == KeyEvent.KEYCODE_BACK) {
                    true
                } else false
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = ShopsAdapter(requireContext(),list,this)
        binding.shopRv.layoutManager = LinearLayoutManager(requireContext())
        binding.shopRv.adapter = adapter

    }

    private fun setupObservers() {
        viewModel.shops.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Resource.Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.shimmerViewContainer.visibility = View.GONE
                    if (!it.data.isNullOrEmpty()){
                        list = it.data as ArrayList<Shop>
                        adapter.setItems(it.data)

                    }
                }
                Resource.Status.ERROR ->{
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
                Resource.Status.LOADING ->{
                    binding.progressBar.visibility = View.VISIBLE
                    binding.shimmerViewContainer.visibility = View.VISIBLE
                }

            }
        })
    }

    override fun onClickedShops(shopId: String) {
        findNavController().navigate(
            R.id.action_charactersFragment_to_characterDetailFragment,
            bundleOf("id" to shopId)
        )
        Constant.gotoDetailsPage = 2
    }

   override fun onPause() {
        super.onPause()
        binding.shimmerViewContainer.stopShimmerAnimation()
    }

  override fun onResume() {
        super.onResume()
        binding.shimmerViewContainer.startShimmerAnimation()
    }

    fun saveArrayList(list: ArrayList<String?>, key: String?) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor: SharedPreferences.Editor = prefs.edit()
        val gson = Gson()
        val json: String = gson.toJson(list)
        editor.putString(key, json)
        editor.apply()
    }




}
