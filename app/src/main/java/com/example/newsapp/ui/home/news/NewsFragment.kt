package com.example.newsapp.ui.home.news

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.newsapp.api.Constant
import com.example.newsapp.api.model.newsResponse.News
import com.example.newsapp.api.model.sourcesResponse.Sources
import com.example.newsapp.databinding.FragmentNewsBinding
import com.example.newsapp.ui.FullNewsActivity
import com.example.newsapp.ui.ViewError
import com.example.newsapp.ui.showMessage
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class NewsFragment : Fragment() {
    lateinit var viewBinding: FragmentNewsBinding
    lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[NewsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentNewsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initObservers()
        viewModel.getNewsSources()

    }

    private fun initObservers() {
//        viewModel.shouldShowLoading
//            .observe(viewLifecycleOwner)
//            { value -> viewBinding.progressBar.isVisible = value }
        viewModel.soucrcesLiveData
            .observe(viewLifecycleOwner) {
                bindTabs(it)
            }
        viewModel.newsLiveData
            .observe(viewLifecycleOwner) {
                adapter.bindNews(it)
            }
        viewModel.viewError
            .observe(viewLifecycleOwner) {
                handleError(it)
            }
    }

    var adapter = NewsAdapter()

    private fun initViews() {
        viewBinding.vm = viewModel
        viewBinding.lifecycleOwner = this
        viewBinding.recyclerView.adapter = adapter
        adapter.onItemClickListner = object :NewsAdapter.OnItemClickListner{
            override fun onClick(position: Int, news: News) {
                showData(news)
            }

        }

    }


     fun showData(news: News) {
        val intent = Intent(requireContext(), FullNewsActivity::class.java)
        intent.putExtra(Constant.OBJ_KEY,news)
        startActivity(intent)
    }

    private fun bindTabs(sources: List<Sources?>?) {
        if (sources == null) return
        sources.forEach { sources ->
            val tab = viewBinding.tabLayout.newTab()
            tab.text = sources?.name
            tab.tag = sources
            viewBinding.tabLayout.addTab(tab)
        }
        viewBinding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val source = tab?.tag as Sources
                viewModel.getNews(source.id)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                val source = tab?.tag as Sources
                viewModel.getNews(source.id)
            }

        })
        viewBinding.tabLayout.getTabAt(0)?.select()

    }


    fun handleError(viewError: ViewError) {
        showMessage(message = viewError.message ?: viewError.t?.localizedMessage
        ?: "something Wrong",
            posActionName = "Try Again",
            posAction = { dialogInterface, i ->
                dialogInterface.dismiss()
                viewError.onTryAgainClickListener?.onTryAgainClicked()
            },
            negActionName = "Cancel",
            negAction = { dialogInterface, i ->
                dialogInterface.dismiss()
            })
    }
}