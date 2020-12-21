package com.romeoautomation;

import java.io.File;
import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.GridRegistry;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.listeners.TestSessionListener;
import org.openqa.grid.selenium.proxy.DefaultRemoteProxy;

public class ExtendedProxy extends DefaultRemoteProxy implements TestSessionListener {

    private  String TENANT_ID = "tenantId";
    private  String TEST_ID = "testId";
    private  String RECORD_VIDEO = "recordVideo";
    private static ScreenRecorder screenRecorder;
    private  Boolean record;
    private String recordingName;
    public ExtendedProxy(RegistrationRequest arg0, GridRegistry arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    // @Override
    // public void beforeCommand(TestSession session, HttpServletRequest request,
    //                         HttpServletResponse response) {
    // System.out.println("Selenium Extending Grid - Before Command");
    // System.out.println("Method " +request.getMethod());
    // System.out.println("getRequestURI " +request.getRequestURI());
    // System.out.println("Session "+session.toString());
    
    // }
 
    // @Override
    // public void afterCommand(TestSession session, HttpServletRequest request,
    //                         HttpServletResponse response) {
    // System.out.println("Romeo Automation Grid - After Command");
    // }
    
    @Override
    public void beforeSession(TestSession session){
        System.out.println("Romeo Automation Grid - Before Session");
        try{
            String testId = session.getRequestedCapabilities().get(TEST_ID).toString();
            String tenantId = session.getRequestedCapabilities().get(TENANT_ID).toString();
            recordingName = testId + "-" +  tenantId;
            
        record = session.getRequestedCapabilities().get(RECORD_VIDEO).toString().toLowerCase().trim().equalsIgnoreCase("true");
        System.out.println(record);
        if (record == true) {
            System.out.println("Beginning video recording"); 
            screenRecorder = new ScreenRecorder();
            screenRecorder.startRecording(recordingName);
            System.out.println("Video Recording value is "+record.toString());
        }
        }catch(Exception ex)
        {
            System.out.println(ex.getMessage());

        }
    }
    
@Override
public void afterSession(TestSession session){
    try{

    if(record == true)
    {String BUCKET_NAME = "romeo-test-recordings";
        System.out.println("Stopping Video recording");
        //save file using tenantid and test id;      
        screenRecorder.stopRecording();
        //upload the file to S3 bucket
        File dir = new File(System.getProperty("user.dir"), "mp4Result");    
        Path path = dir.toPath().resolve(recordingName + ".mp4");
        Regions clientRegion = Regions.US_EAST_2;
        try{
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion(clientRegion)
            .build(); 
            PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, recordingName + ".mp4", path.toFile());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("video/mp4");

            request.setMetadata(metadata);
            s3Client.putObject(request);    
        }catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process 
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
    }
}catch(Exception ex)
{
    System.out.println(ex.getMessage());

}
           
    }
}
