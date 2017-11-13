package io.github.jamiesanson.broker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.github.jamiesanson.broker.compiler.BrokerRepoResolver
import io.github.jamiesanson.broker.repo.TestRepo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity: AppCompatActivity() {

    // TODO make this injectable, and not have to reference generated class
    lateinit var repo: TestRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        repo = BrokerRepoResolver(application as BrokerSampleApp).get(TestRepo::class.java)

        transientButton.setOnClickListener {
            repo.transientTestString().get()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { string ->
                        Toast.makeText(this, string, Toast.LENGTH_LONG).show()
                    }
        }

        persistentButton.setOnClickListener {
            repo.persistentTestInt().get()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { int ->
                        Toast.makeText(this, int.toString(), Toast.LENGTH_LONG).show()
                    }
        }
    }

}