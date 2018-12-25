package net.stripe.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_run_once.*
import net.stripe.lib.toRunOnceTask
import net.stripe.sample.util.showToast

class RunOnceActivity : AppCompatActivity() {

    private val runnable = Runnable {
        showToast(this, "toast once")
    }.toRunOnceTask()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_once)

        btn_once.setOnClickListener {

            // instant run
            //runnable.run()

            // run after delay
            it.postDelayed(runnable, 1000)
        }
    }
}
