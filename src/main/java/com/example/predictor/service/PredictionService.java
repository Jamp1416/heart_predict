package com.example.predictor.service;

import com.example.predictor.model.PatientData;
import weka.classifiers.Classifier;
import weka.core.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class PredictionService {

    private Classifier model;
    private ArrayList<Attribute> attributes;

    @Autowired
    private GeminiService geminiService;

    public PredictionService() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("heart.model"); // Asegúrate del nombre correcto
            ObjectInputStream ois = new ObjectInputStream(is);
            model = (Classifier) ois.readObject();
            ois.close();

            // Definir los atributos exactamente como en el .arff
            attributes = new ArrayList<>();
            attributes.add(new Attribute("age")); // REAL
            attributes.add(new Attribute("sex", List.of("F", "M"))); // NOMINAL
            attributes.add(new Attribute("chestpaintype", List.of("ASY", "ATA", "NAP", "TA"))); // NOMINAL
            attributes.add(new Attribute("restingbp")); // REAL
            attributes.add(new Attribute("cholesterol")); // REAL
            attributes.add(new Attribute("fastingbs", List.of("0", "1"))); // NOMINAL
            attributes.add(new Attribute("restingecg", List.of("LVH", "Normal", "ST"))); // NOMINAL
            attributes.add(new Attribute("maxhr")); // REAL
            attributes.add(new Attribute("exerciseangina", List.of("N", "Y"))); // NOMINAL
            attributes.add(new Attribute("oldpeak")); // REAL
            attributes.add(new Attribute("st_slope", List.of("Down", "Flat", "Up"))); // NOMINAL
            attributes.add(new Attribute("heartdisease", List.of("0", "1"))); // CLASE

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String predict(PatientData data) {
        try {
            Instances dataset = new Instances("TestInstance", attributes, 1);
            dataset.setClassIndex(dataset.numAttributes() - 1);

            DenseInstance instance = new DenseInstance(dataset.numAttributes());
            instance.setDataset(dataset);

            instance.setValue(attributes.get(0), data.getAge());
            instance.setValue(attributes.get(1), data.getSex());
            instance.setValue(attributes.get(2), data.getChestpaintype());
            instance.setValue(attributes.get(3), data.getRestingbp());
            instance.setValue(attributes.get(4), data.getCholesterol());
            instance.setValue(attributes.get(5), data.getFastingbs());
            instance.setValue(attributes.get(6), data.getRestingecg());
            instance.setValue(attributes.get(7), data.getMaxhr());
            instance.setValue(attributes.get(8), data.getExerciseangina());
            instance.setValue(attributes.get(9), data.getOldpeak());
            instance.setValue(attributes.get(10), data.getSt_slope());

            double result = model.classifyInstance(instance);
            return (result == 1.0) ? "ALTO RIESGO DE ENFERMEDAD CARDÍACA" : "BAJO RIESGO DE ENFERMEDAD CARDÍACA";

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR EN PREDICCIÓN";
        }
    }

    public String getGeminiSuggestion(String predictionText, PatientData data) {
        // Llamar al servicio GeminiService para obtener la sugerencia
        String summary = String.format(""" 
            Edad: %.0f, Sexo: %s, Tipo de Dolor: %s, Presión en Reposo: %.1f, Colesterol: %.1f,
            FastingBS: %s, ECG: %s, Frecuencia Máxima: %.1f, Angina: %s, Oldpeak: %.2f, ST Slope: %s
        """, data.getAge(), data.getSex(), data.getChestpaintype(), data.getRestingbp(), data.getCholesterol(),
                data.getFastingbs(), data.getRestingecg(), data.getMaxhr(), data.getExerciseangina(), data.getOldpeak(), data.getSt_slope());

        return geminiService.getSuggestion(predictionText, summary);
    }
}
