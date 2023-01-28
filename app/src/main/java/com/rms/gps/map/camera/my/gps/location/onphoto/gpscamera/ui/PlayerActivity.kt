package com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.BuildConfig
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.R
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.databinding.ActivityPlayerBinding
import java.io.File

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class PlayerActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE){
        ActivityPlayerBinding.inflate(layoutInflater)
    }
    private val exoPlayer by lazy{
        ExoPlayer.Builder(this).build()
    }

    override fun  onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent.getStringExtra("video_path")?.let { filePath ->
            val videoFile = File(filePath)
            if(videoFile.exists()) {
                val uri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    videoFile
                )
                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer.addMediaItem(mediaItem)
                binding.playerView.player = exoPlayer
                binding.playerView.hideController()
                binding.playerView.useController = false
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                exoPlayer.prepare()
                exoPlayer.play()
                binding.playerView.setOnClickListener {
                    if(exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                }
            }
        }?: kotlin.run {
            finish()
        }

    }
}