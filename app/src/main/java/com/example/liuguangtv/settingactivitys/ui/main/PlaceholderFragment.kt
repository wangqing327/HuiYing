package com.example.liuguangtv.settingactivitys.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.liuguangtv.AppBroadcast
import com.example.liuguangtv.R
import com.example.liuguangtv.WebBrosWer
import com.example.liuguangtv.databinding.FragmentJlScBinding
import com.example.liuguangtv.utils.SimpleDividerItemDecoration
import com.example.liuguangtv.utils.Utils
import com.example.liuguangtv.utils.database.DatabaseFactory
import com.example.liuguangtv.utils.database.History
import com.example.liuguangtv.utils.database.HistoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment(), Observer<MutableList<History>>,
    BaseQuickAdapter.OnItemClickListener {
    /**
     * 实例序号 0播放记录 1我的收藏
     */
    private var index = 0
    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentJlScBinding? = null
    private lateinit var mAdapter: AdapterJl
    private val binding get() = _binding!!
    private lateinit var database: DatabaseFactory
    private lateinit var dao: HistoryDao

    /**
     * 当前是否于编辑模式，如果处于编辑模式，点击事件将切换checkbox的选中状态
     */
    private var edit = false
    private val ARG_SECTION_NUMBER = "section_number"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = DatabaseFactory.getDataBase(requireContext())
        dao = database.getHistoryDao()
        index = arguments?.getInt(ARG_SECTION_NUMBER) ?: 0
        pageViewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJlScBinding.inflate(inflater, container, false)

        registerForContextMenu(binding.recyclerview)
        //列表设置
        val stManager = LinearLayoutManager(requireContext())
        binding.recyclerview.layoutManager = stManager
        //分隔线设置
        binding.recyclerview.addItemDecoration(
            SimpleDividerItemDecoration(requireContext(), 4, 8)
        )
        mAdapter = AdapterJl(R.layout.historylayout)
        binding.recyclerview.adapter = mAdapter
        mAdapter.onItemClickListener = this
        mAdapter.onItemChildClickListener =
            BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
                (adapter.getItem(position) as History).checked = (view as CheckBox).isChecked
            }
        binding.recyclerview.setOnCreateContextMenuListener { contextMenu, _, _ ->
            createMenu(contextMenu)
        }

        when (index) {
            0 -> {
                pageViewModel.history.observe(requireActivity(), this::onChanged)
                lifecycleScope.launch(Dispatchers.IO) {
                    val data = dao.queryAllData(index)
                    pageViewModel.history.postValue(data)
                }
            }
            1 -> {
                pageViewModel.favorite.observe(requireActivity(), this::onChanged)
                lifecycleScope.launch(Dispatchers.IO) {
                    val data = dao.queryAllData(index)
                    pageViewModel.favorite.postValue(data)
                }
            }
        }
        binding.floatingActionButton.setOnClickListener {
            if (mAdapter.itemCount < 1) {
                return@setOnClickListener
            }
            edit = edit.not()
            mAdapter.changeEditMode(edit)
            if (edit) {
                binding.constraintLayout2.visibility = View.VISIBLE
            } else {
                binding.constraintLayout2.visibility = View.GONE
                binding.allselect.isChecked = false
            }

        }
        binding.allselect.setOnCheckedChangeListener { _, b ->
            if (b) {
                mAdapter.setAllItemChecked()
            } else {
                mAdapter.setAllItemNotChecked()
            }
        }
        binding.qx.setOnClickListener {
            edit = false
            mAdapter.unEditMode()
            binding.allselect.isChecked = false
            binding.constraintLayout2.visibility = View.GONE
        }
        binding.sc.setOnClickListener {
            //当前没有选中状态的项目
            val list = mAdapter.getAllCheckedItems()
            if (list.size < 1) {
                return@setOnClickListener
            }
            lifecycleScope.launch(Dispatchers.IO) {
                list.forEach {
                    dao.deleteData(it)
                }
                withContext(Dispatchers.Main) {
                    mAdapter.removeAllSelectedItem()
                    Utils.showToast(requireContext(), R.string.qcwc)
                }
            }
            if (mAdapter.itemCount < 1) {
                edit = false
                binding.allselect.isChecked = false
                binding.constraintLayout2.visibility = View.GONE
                mAdapter.unEditMode()
            }
        }
        return binding.root
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onChanged(t: MutableList<History>?) {
        mAdapter.setNewData(t)
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        if (edit) {
            mAdapter.changeItemChecked(position)
        } else {
            val item = adapter.getItem(position) as History
            val intent = Intent(requireContext(), WebBrosWer::class.java)
            intent.putExtra("url", item.url)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as RecyclerViewWithContextMenu.RecyclerViewContextInfo
        if (info.position < 0) {
            return true
        }
        if (!userVisibleHint) {
            //这地方可不敢返回true,返回false给下一个fragment接收消息
            return false
        }
        when (item.itemId) {
            //添加到收藏
            R.id.addfav -> {
                val history = mAdapter.getItem(info.position)
                if (history != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val id = dao.queryDataByUrl(history.url, 1)
                        val data = History(id, history.title, history.url, 1)
                        dao.insertData(data)
                        if (id == 0) {
                            //pageViewModel.addHistoryValue(data)
                                //不这样干，会导致删除不了
                            pageViewModel.favorite.postValue(dao.queryAllData(1))
                        }
                    }
                }
            }
            R.id.deleteItem -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    val history = mAdapter.getItem(info.position)
                    history?.apply { dao.deleteData(history) }
                    withContext(Dispatchers.Main) {
                        mAdapter.remove(info.position)
                    }
                }
            }
            //添加到首页列表
            R.id.addHome -> {
                val history = mAdapter.getItem(info.position)
                history?.let {
                    val bundle = Bundle()
                    bundle.putSerializable("history", it)
                    val intent = Intent()
                    intent.action = AppBroadcast.BroadCastString
                    intent.putExtra("bundle", bundle)
                    requireActivity().sendBroadcast(intent)
                }

            }
        }
        return true
    }

    private fun createMenu(menu: ContextMenu) {
        if (edit) {
            //编辑模式下禁止弹出菜单
            return
        }
        MenuInflater(requireContext()).inflate(R.menu.lsjl, menu)
        if (index == 1) {
            menu.removeItem(R.id.addfav)
        }
    }
}