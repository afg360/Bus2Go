package dev.mainhq.bus2go.presentation.choose_direction;

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import dev.mainhq.bus2go.presentation.base.BaseActivity
import dev.mainhq.bus2go.R
import dev.mainhq.bus2go.domain.entity.ExoBusItem
import dev.mainhq.bus2go.domain.entity.ExoBusRouteInfo
import dev.mainhq.bus2go.domain.entity.ExoTrainItem
import dev.mainhq.bus2go.domain.entity.ExoTrainRouteInfo
import dev.mainhq.bus2go.domain.entity.RouteInfo
import dev.mainhq.bus2go.domain.entity.StmBusItem
import dev.mainhq.bus2go.domain.entity.StmBusRouteInfo
import dev.mainhq.bus2go.presentation.Bus2GoApplication
import dev.mainhq.bus2go.presentation.choose_stop.ChooseStop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.IllegalStateException
import dev.mainhq.bus2go.presentation.utils.ExtrasTagNames
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first


//todo
//change appbar to be only a back button
//todo may make it a swappable ui instead of choosing button0 or 1
class ChooseDirection : BaseActivity() {


    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_direction)

        val routeInfo = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			intent.extras?.getParcelable(ExtrasTagNames.ROUTE_INFO, RouteInfo::class.java)
		} else {
            @Suppress("DEPRECATION")
            intent.extras?.getParcelable(ExtrasTagNames.ROUTE_INFO)
        }) ?: throw IllegalStateException("Expected a non null RouteInfo passed")

        val chooseDirectionViewModel: ChooseDirectionViewModel by viewModels{
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                    if (modelClass.isAssignableFrom(ChooseDirectionViewModel::class.java)){
                        return ChooseDirectionViewModel(
                            routeInfo = routeInfo,
                            getDirections = (this@ChooseDirection.application as Bus2GoApplication).appContainer.getDirections,
                            getStopNames = (this@ChooseDirection.application as Bus2GoApplication).appContainer.getStopNames,
                        ) as T
                    }
                    throw IllegalArgumentException("Gave wrong ViewModel class")
                }
            }
        }

		//set a loading screen first before displaying the correct buttons
        val busNumView: MaterialTextView = findViewById(R.id.chooseBusNum)
        val busNameView: MaterialTextView = findViewById(R.id.chooseBusDir)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                chooseDirectionViewModel.isUnidirectional.collect{ isUnidirectional ->
                    val intent = Intent(applicationContext, ChooseStop::class.java)
                    val leftDescr = findViewById<MaterialTextView>(R.id.description_route_0)
                    val rightDescr = findViewById<MaterialTextView>(R.id.description_route_1)

                    withContext(Dispatchers.Main) {
                        busNumView.text = routeInfo.routeId
                        busNameView.text = routeInfo.routeName
                        when(routeInfo){
                            is ExoBusRouteInfo -> {
                                val leftDir = chooseDirectionViewModel.leftDirection.filterNotNull().first() as List<ExoBusItem>
                                assert(leftDir.isNotEmpty())

                                if (isUnidirectional == true) {
                                    intent.putExtra(ExtrasTagNames.TRANSIT_DATA, leftDir.toTypedArray())
                                    Toast.makeText(this@ChooseDirection,"This bus line only contains 1 direction",Toast.LENGTH_SHORT).show()
                                    finish()
                                    startActivity(intent)
                                }
                                else if (isUnidirectional == false) {
                                    val rightDir = chooseDirectionViewModel.rightDirection.filterNotNull().first() as List<ExoBusItem>
                                    assert(rightDir.isNotEmpty())

                                    findViewById<MaterialTextView>(R.id.description_route_0).text = leftDir.first().headsign
                                    findViewById<MaterialButton>(R.id.route_0).setOnClickListener {
                                        intent.putExtra(ExtrasTagNames.TRANSIT_DATA, leftDir.toTypedArray())
                                        startActivity(intent)
                                    }
                                    findViewById<MaterialTextView>(R.id.description_route_1).text = rightDir.first().headsign
                                    findViewById<MaterialButton>(R.id.route_1).setOnClickListener {
                                        intent.putExtra(ExtrasTagNames.TRANSIT_DATA, rightDir.toTypedArray())
                                        startActivity(intent)
                                    }
                                    leftDescr.text = getString(R.string.from_to, leftDir.first().stopName, leftDir.last().stopName)
                                    rightDescr.text = getString(R.string.from_to, rightDir.first().stopName, rightDir.last().stopName)
                                }
                            }
                            is ExoTrainRouteInfo -> {
                                val leftDir = chooseDirectionViewModel.leftDirection.filterNotNull().first() as List<ExoTrainItem>
                                if (isUnidirectional == false){
                                    val rightDir = chooseDirectionViewModel.rightDirection.filterNotNull().first() as List<ExoTrainItem>

                                    findViewById<MaterialTextView>(R.id.description_route_0).text =
                                        getString(R.string.train_direction, leftDir.first().direction)
                                    findViewById<MaterialButton>(R.id.route_0).setOnClickListener {
                                        intent.putExtra(ExtrasTagNames.TRANSIT_DATA, leftDir.toTypedArray())
                                        startActivity(intent)
                                    }

                                    findViewById<MaterialTextView>(R.id.description_route_1).text =
                                        getString(R.string.train_direction, rightDir.first().direction)
                                    findViewById<MaterialButton>(R.id.route_1).setOnClickListener {
                                        intent.putExtra(ExtrasTagNames.TRANSIT_DATA, rightDir.toTypedArray())
                                        startActivity(intent)
                                    }
                                    leftDescr.text = getString(R.string.from_to, leftDir.first().stopName, leftDir.last().stopName)
                                    rightDescr.text = getString(R.string.from_to, rightDir.first().stopName, rightDir.last().stopName)
                                }
                                else if (isUnidirectional == true)
                                    throw IllegalStateException("Unexpected data: ExoTrain being unidirectional...")
                            }
                            is StmBusRouteInfo -> {
                                val leftDir = chooseDirectionViewModel.leftDirection.filterNotNull().first() as List<StmBusItem>
                                if (isUnidirectional == false) {
                                    val rightDir = chooseDirectionViewModel.rightDirection.filterNotNull().first() as List<StmBusItem>

                                    val leftButton = findViewById<MaterialButton>(R.id.route_0)
                                    val rightButton = findViewById<MaterialButton>(R.id.route_1)
                                    leftButton.setOnClickListener {
                                        intent.putExtra(ExtrasTagNames.TRANSIT_DATA, leftDir.toTypedArray())
                                        startActivity(intent)
                                    }
                                    rightButton.setOnClickListener {
                                        intent.putExtra(ExtrasTagNames.TRANSIT_DATA, rightDir.toTypedArray())
                                        startActivity(intent)
                                    }
                                    leftDescr.text = getString(R.string.from_to, leftDir.first().stopName, leftDir.last().stopName)
                                    rightDescr.text = getString(R.string.from_to, rightDir.first().stopName, rightDir.last().stopName)

                                    if (leftDir.first().direction == "est" || leftDir.first().direction == "ouest") {
                                        leftButton.text = getString(R.string.west)
                                        rightButton.text = getString(R.string.east)
                                    }
                                    else {
                                        leftButton.text = getString(R.string.north)
                                        rightButton.text = getString(R.string.south)
                                    }
                                }
                                else if (isUnidirectional == true)
                                    throw IllegalStateException("Unexpected data: StmBus being unidirectional...")
                            }
                        }
                    }
                }
            }
        }
    }
}
