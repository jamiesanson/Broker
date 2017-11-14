package io.github.jamiesanson.broker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.github.jamiesanson.broker.repo.SpaceXRepo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.main_activity.*
import javax.inject.Inject

class MainActivity: AppCompatActivity() {

    @Inject
    lateinit var repo: SpaceXRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        (application as BrokerSampleApp).component.inject(this)

        latestLaunchButton.setOnClickListener {
            getLatestLaunch()
        }

        pastLaunchesButton.setOnClickListener {
            getPastLaunches()
        }
    }

    private fun getLatestLaunch() {
        repo.latestLaunch().get()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { launch ->
                    Log.d("MainActivity","Got Latest: $launch")

                }
    }

    private fun getPastLaunches() {
        repo.pastLaunches().get()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { launch ->
                    Log.d("MainActivity","Got Past: $launch")

                }
    }



}