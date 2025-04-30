package com.mx.ebany.embalsadordeliquidos.ui.dialogs

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.mx.ebany.embalsadordeliquidos.R
import com.mx.ebany.embalsadordeliquidos.core.room.DataConfiguration
import com.mx.ebany.embalsadordeliquidos.databinding.FragmentProcesDialogBinding
import com.mx.ebany.embalsadordeliquidos.tools.Constants
import com.mx.ebany.embalsadordeliquidos.ui.home.viewModel.ViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class ProcesDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentProcesDialogBinding
    private val viewModel: ViewModel by sharedViewModel<ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProcesDialogBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setInitialValues()
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        viewModel.getConfiguration.observe(viewLifecycleOwner){
            if(it != null){
                binding.etConstLlenado.setText(it.constanteLlenado.toString())
                binding.etDelayMotor.setText(it.delayMotor.toString())
            }
        }

        viewModel.dataSave.observe(viewLifecycleOwner){
            if (it != null){
                Toast.makeText(requireActivity(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                viewModel.getDataConfiguration()
            }
        }

    }

    private fun setListeners() {
        binding.btSave.setOnClickListener {
            verifyData()
        }
    }

    private fun setInitialValues() {
        viewModel.getDataConfiguration()
    }

    private fun verifyData(){
        var cont = 0
        if(binding.etDelayMotor.text.isNullOrEmpty())  cont ++
        if(binding.etConstLlenado.text.isNullOrEmpty())  cont ++
        if(cont>0){
            Toast.makeText(requireActivity(), "Debes ingresar datos validos", Toast.LENGTH_LONG).show()
        }else{
            saveData()
        }
    }

    private fun saveData(){
        try {
            val data = DataConfiguration()
            Log.e("DATOS", "${binding.tvDelayMotor}")
            data.delayMotor = binding.etDelayMotor.text.toString()
            data.constanteLlenado = binding.etConstLlenado.text.toString()
            viewModel.addConfiguration(data, true)
        }catch (e: Error){
            Toast.makeText(requireActivity(), e.message, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

}