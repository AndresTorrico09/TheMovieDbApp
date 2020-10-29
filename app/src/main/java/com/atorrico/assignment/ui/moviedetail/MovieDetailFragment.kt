package com.atorrico.assignment.ui.moviedetail

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.atorrico.assignment.R
import com.atorrico.assignment.data.entities.Movie
import com.atorrico.assignment.databinding.FragmentMovieDetailBinding
import com.atorrico.assignment.utils.Constants.BASE_URL_IMAGES
import com.atorrico.assignment.utils.Resource
import com.atorrico.assignment.utils.autoCleared
import com.atorrico.assignment.utils.getDominantColor
import com.atorrico.assignment.utils.getYear
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_movie_detail.*
import kotlinx.android.synthetic.main.fragment_movie_detail.view.*
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MovieDetailFragment : Fragment() {

    private var binding: FragmentMovieDetailBinding by autoCleared()
    private val viewModel: MovieDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply {
            duration = 300.toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getInt("id")?.let { viewModel.start(it) }
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.movie.observe(viewLifecycleOwner, {
            bindMovie(it!!)
            binding.progressBar.visibility = View.GONE
            binding.movieCl.visibility = View.VISIBLE
        })
    }

    private fun bindMovie(movie: Movie) {
        binding.tvTitle.text = movie.title
        binding.tvYear.text = getYear(movie.release_date)
        binding.tvOverview.text = movie.overview
        if (movie.subscribe){
            binding.btnSuscribe.text = resources.getString(R.string.suscribed)
            binding.btnSuscribe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
            binding.btnSuscribe.setTextColor(ContextCompat.getColor(requireContext(), R.color.transparent))
        }
        else{
            binding.btnSuscribe.text = resources.getString(R.string.suscribe)
            binding.btnSuscribe.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            binding.btnSuscribe.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }


        Glide.with(this)
            .asBitmap()
            .load(BASE_URL_IMAGES + movie.poster_path)
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val dominantColor = getDominantColor(resource)
                    view?.nested_detail?.setBackgroundColor(dominantColor)
                    setToolbarProperties(dominantColor, resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })

        btnSuscribe.setOnClickListener{
            lifecycleScope.launch {
                movie.subscribe = !movie.subscribe
                viewModel.update(movie)
            }
        }
    }

    private fun setToolbarProperties(dominantColor: Int, resource: Bitmap) {
        val collapsing: View? = activity?.findViewById(R.id.collapsing_toolbar_layout)
        collapsing?.imgToolbar?.setImageBitmap(resource)
        collapsing?.imgToolbar?.visibility = View.VISIBLE
        collapsing?.setBackgroundColor(dominantColor)
    }

}
