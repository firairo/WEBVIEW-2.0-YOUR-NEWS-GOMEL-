package by.lexshi.lexshi

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var networkAvailable = false
    lateinit var mWebView: WebView
    lateinit var drawerLayout:DrawerLayout

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        var url =getString(R.string.website_url)

        mWebView = findViewById(R.id.webView)
        val webSettings =mWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.setAppCacheEnabled(false)

        loadWebSite (mWebView, url, applicationContext)
        swipeRefreshLayout.setColorSchemeResources(R.color.colorRed, R.color.colorBlue, R.color.colorGreen)
        swipeRefreshLayout.apply {
            setOnRefreshListener {
                if (mWebView.url != null) url = mWebView.url
                loadWebSite(mWebView, url, applicationContext)
            }
            setOnChildScrollUpCallback { swipeRefreshLayout, view -> mWebView.getScrollY()  > 0}
        }
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

         drawerLayout = findViewById(R.id.drawer_layout) // Инициализируем дроверлйаут

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) //Задаем значек бургера
        drawerLayout.addDrawerListener(toggle) // привязываем бургер к двроверлйауту
        toggle.syncState() // шоб синхронизировалось:)


        val navView : NavigationView = findViewById(R.id.nav_view) // Инициализируем навигейшенвью
        navView.setNavigationItemSelectedListener(this) //Задаем слушателя (слушателем будет активити)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_home -> {
                val url=getString(R.string.website_url)
                loadWebSite (mWebView, url, applicationContext)

            }
            R.id.nav_gallery -> {
                val url=getString(R.string.cultura)
                loadWebSite (mWebView, url, applicationContext)
            }
            R.id.nav_slideshow -> {
                val url=getString(R.string.politics)
                loadWebSite (mWebView, url, applicationContext)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

// Функция загрузки сайта, есть есть соединение
    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadWebSite(mWebView: WebView, url: String, context: Context){

        progressbar.visibility=View.VISIBLE
        networkAvailable=isNetworkAvailable(context)
        mWebView.clearCache(true)
        if (networkAvailable){
            wvVisible(mWebView)
            mWebView.webViewClient = MyWebViewClient()
            mWebView.loadUrl(url)
        }
        else{
            wvGone(mWebView)
            swipeRefreshLayout.isRefreshing = false
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    //Показ вебвью
    private fun wvVisible (mWebView: WebView){
        mWebView.visibility = View.VISIBLE
        tvCheckConnection.visibility =View.GONE
    }
    //Скрыть вебвью, показ ошибки, скрыть прогрессбар
    private fun wvGone (mWebView: WebView){
        mWebView.visibility = View.GONE
        tvCheckConnection.visibility =View.VISIBLE
        progressbar.visibility = View.GONE
    }



    // Проверка интернет-соединения
@Suppress( "DEPRECATION")
    private fun isNetworkAvailable (context: Context):Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            return if (Build.VERSION.SDK_INT > 22) {
                val an = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(an) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
            val a =cm.activeNetworkInfo ?: return false
                a.isConnected && (a.type == ConnectivityManager.TYPE_WIFI || a.type == ConnectivityManager.TYPE_MOBILE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    return false
 }

    private fun  onLoadCompleted(){
        swipeRefreshLayout.isRefreshing = false
        progressbar.visibility = View.GONE
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private inner class MyWebViewClient : WebViewClient(){

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {

            val url = request?.url.toString()
            return urlOverride(url)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return urlOverride(url)
        }

        private fun urlOverride(url: String): Boolean {
            progressbar.visibility = View.VISIBLE
            networkAvailable = isNetworkAvailable(applicationContext)

            if (networkAvailable) {
                if (Uri.parse(url).host == getString(R.string.website_url)) return false
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                onLoadCompleted()
                return true
            } else {
                wvGone(webView)
                return false
            }
        }
        @Suppress("DEPRECATION")
        override fun onReceivedError(
            view: WebView?,
            errorCode: Int,
            description: String?,
            failingUrl: String?
        ) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            if (errorCode == 0) {
                view?.visibility = View.GONE
                tvCheckConnection.visibility = View.VISIBLE
                onLoadCompleted()
            }
        }
        @TargetApi(Build.VERSION_CODES.M)
        @Suppress("DEPRECATION")
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, error!!.errorCode, error.description.toString(), request!!.url.toString())

        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onLoadCompleted()
        }

    }



}