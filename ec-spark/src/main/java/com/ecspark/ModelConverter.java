package com.ecspark;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import org.apache.spark.sql.SparkSession;

import java.io.File;
import java.io.IOException;

public class ModelConverter {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("EC Spark Model Converter")
                .master("local[*]")
                .getOrCreate();

        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        // Load the trained Linear Regression model
        String modelPath = "c:/enterprise/tmp/model/spark-lr";
        LinearRegressionModel model = LinearRegressionModel.load(jsc.sc(), modelPath);

        // Extract model parameters
        Vector weights = model.weights();
        double intercept = model.intercept();

        // Create a simple POJO to hold model parameters
        LinearRegressionModelData modelData = new LinearRegressionModelData(weights.toArray(), intercept);

        // Convert model parameters to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(modelData);
            // Save JSON to file
            objectMapper.writeValue(new File("c:/enterprise/tmp/model/spark-lr/linear_regression_model.json"), modelData);
            System.out.println("Model saved as JSON: " + jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Stop Spark context
        jsc.close();
        spark.stop();
    }
}

class LinearRegressionModelData {
    private double[] weights;
    private double intercept;

    public LinearRegressionModelData(double[] weights, double intercept) {
        this.weights = weights;
        this.intercept = intercept;
    }

    public double[] getWeights() {
        return weights;
    }

    public void setWeights(double[] weights) {
        this.weights = weights;
    }

    public double getIntercept() {
        return intercept;
    }

    public void setIntercept(double intercept) {
        this.intercept = intercept;
    }
}
