#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}

#end
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.androidthings.lantern.Channel

class ${NAME}: Channel() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = TextView(context)
        view.text = "Hello world from my new Lantern channel!"
        view.textSize = 30f
        view.gravity = Gravity.CENTER
        return view

        // alternatively, you can load from a layout file
        // return inflater.inflate(R.layout.my_channel, viewGroup, false)
    }
}
