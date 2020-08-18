package np.com.manishtuladhar.emojiplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class EmojiPlayer {

    private static final String TAG = "EmojiPlayer";

    //threshold
    private static final double SMILING_PROB_THRESHOLD = .15;
    private static final double EYE_OPEN_THRESHOLD = .5;
    /**
     * Function to detect faces
     */
    public static void detectFaces(Context context, Bitmap picture)
    {
        //create face detector
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        //frame
        Frame frame = new Frame.Builder().setBitmap(picture).build();

        //detect the faces
        SparseArray<Face> faces = detector.detect(frame);

        Log.e(TAG, "detectFaces: number of faces detected" +faces.size() );

        if(faces.size() == 0)
        {
            Toast.makeText(context, "No faces detected", Toast.LENGTH_SHORT).show();
        }
        else{
            for(int i =0;i<faces.size();i++)
            {
                Face face = faces.valueAt(i);
                getClassifications(face);

            }
        }
        detector.release();
    }

    /**
     * Get the classification probability
     */
    private static void getClassifications(Face face)
    {
        //log all the probabilities
        Log.e(TAG, "getClassifications: smiling prob = "+face.getIsSmilingProbability());
        Log.e(TAG, "getClassifications: left eye open prob = "+face.getIsLeftEyeOpenProbability());
        Log.e(TAG, "getClassifications: right eye open prob = "+face.getIsRightEyeOpenProbability());
    }


}
