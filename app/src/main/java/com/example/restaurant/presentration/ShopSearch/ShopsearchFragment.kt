package com.example.restaurant.presentration.ShopSearch


import android.app.Activity.RESULT_OK
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
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.restaurant.R
import com.example.restaurant.data.entities.Shop
import com.example.restaurant.databinding.ShopSearchFragmentBinding
import com.example.restaurant.domain.AppModule
import com.example.restaurant.presentration.apiSearch.SearchShopViewModel
import com.example.restaurant.presentration.list.ShopAdapter
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
    private val viewModelSearchAPI: SearchShopViewModel by viewModels()
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
        binding.searchNameEt.text.isEmpty().apply {
            setupRecyclerView()
            setupObservers()
        }
        binding.searchNameEt.text.isNotEmpty().apply {
            setupRecyclerView()
            binding.searchNameEt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                  //val value =   s.toString().let { viewModelSearchAPI!!.start(it) }
                   // viewModelSearchAPI!!.keyword = s.toString()
                    //viewModelSearchAPI.start(s.toString())
                     //   setResult()
                         adapter.filter.filter(s)
                    Handler().postDelayed(Runnable
                    {
                        searchHistoryList.add(binding.searchNameEt.text.toString())
                        saveArrayList(searchHistoryList,"search_history_list")
                    }, 3 * 1000
                    )
                    }
            })
        }
        binding.searchHistory.setOnClickListener {

            val intent = Intent(requireContext(),SearchHistoryActivity::class.java)
              startActivityForResult(intent,1)
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

    private fun setResult(){
        viewModelSearchAPI?.character?.observe(viewLifecycleOwner, Observer {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 1) {
            if (resultCode === RESULT_OK) {
                if(data != null){
                    binding.searchNameEt.setText(data.getStringExtra("data"))
//                    adapter.filter.filter(data.getStringExtra("data"))
                    searchHistoryList.add(data.getStringExtra("data"))
                    saveArrayList(searchHistoryList,"search_history_list")
                }
            }
        }

    }


}
