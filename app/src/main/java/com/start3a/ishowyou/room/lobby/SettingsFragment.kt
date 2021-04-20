package com.start3a.ishowyou.room.lobby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.start3a.ishowyou.R
import com.start3a.ishowyou.data.MyApplication
import com.start3a.ishowyou.room.ChatRoomViewModel
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {

    private var viewModel: ChatRoomViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = requireActivity().application!!.let {
            ViewModelProvider(
                requireActivity().viewModelStore,
                ViewModelProvider.AndroidViewModelFactory(it)
            )
                .get(ChatRoomViewModel::class.java)
        }

        val vm = viewModel!!

        switchStopCurWatchedVideos.isChecked = vm.isStopSavingCurWatchedVideo
        switchStopSearchKeywords.isChecked = vm.isStopSearchKeywords

        switchStopCurWatchedVideos.setOnCheckedChangeListener { _, isChecked ->
            vm.isStopSavingCurWatchedVideo = isChecked
            MyApplication.prefs.setString("isStopSavingCurWatchedVideo", isChecked.toString())
        }

        switchStopSearchKeywords.setOnCheckedChangeListener { _, isChecked ->
            vm.isStopSearchKeywords = isChecked
            MyApplication.prefs.setString("isStopSearchKeywords", isChecked.toString())
        }

        btnRemoveCurSavedVideos.setOnClickListener {
            removeCurSavedVideos()
        }

        btnRemoveSearchKeywords.setOnClickListener {
            removeSearchKeywords()
        }
    }

    private fun removeCurSavedVideos() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage("최근 시청 영상 기록을 삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->

                viewModel!!.removeAllCurWatchedVideo()
            }
            .setNegativeButton("취소", null)
            .create().show()
    }

    private fun removeSearchKeywords() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage("최근 영상 검색 기록을 삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->

                viewModel!!.removeAllSearchKeywords()
            }
            .setNegativeButton("취소", null)
            .create().show()
    }
}