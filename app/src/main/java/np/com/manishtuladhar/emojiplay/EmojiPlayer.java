package np.com.manishtuladhar.emojiplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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

    //emoji scale factor
    private static final float EMOJI_SCALE_FACTOR = .9f;


    /**
     * Function to detect faces
     */
    public static Bitmap detectFacesAndEmoji(Context context, Bitmap picture)
    {

        //create face detector
        FaceDetector detector = new FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        //initialized picture
        Bitmap resultBitmap = picture;

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
                Bitmap emojiBitmap;
                switch (getRightEmoji(face))
                {
                    case SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.smile);
                        break;
                    case FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.frown);
                        break;
                    case LEFT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.leftwink);
                        break;
                    case RIGHT_WINK:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.rightwink);
                        break;
                    case LEFT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.leftwinkfrown);
                        break;
                    case RIGHT_WINK_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.rightwinkfrown);
                        break;
                    case CLOSED_EYE_SMILE:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.closed_smile);
                        break;
                    case CLOSED_EYE_FROWN:
                        emojiBitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.closed_frown);
                        break;
                    default:
                        emojiBitmap = null;
                        Toast.makeText(context, "No emoji detected", Toast.LENGTH_SHORT).show();
                }
                resultBitmap = addBitmapToFace(picture,emojiBitmap,face);
            }
        }
        detector.release();
        return resultBitmap;
    }

    /**
     * Get the right emoji using classification probability
     */
    private static Emoji getRightEmoji(Face face)
    {
        //log all the probabilities
        Log.e(TAG, "getRightEmoji: smiling prob = "+face.getIsSmilingProbability());
        Log.e(TAG, "getRightEmoji: left eye open prob = "+face.getIsLeftEyeOpenProbability());
        Log.e(TAG, "getRightEmoji: right eye open prob = "+face.getIsRightEyeOpenProbability());

        boolean smiling = face.getIsSmilingProbability() > SMILING_PROB_THRESHOLD;
        boolean leftEyeClosed = face.getIsLeftEyeOpenProbability() < EYE_OPEN_THRESHOLD;
        boolean rightEyeClosed = face.getIsRightEyeOpenProbability() < EYE_OPEN_THRESHOLD;

        Emoji emoji;
        if(smiling)
        {
            if(leftEyeClosed && !rightEyeClosed)
            {
                emoji = Emoji.LEFT_WINK;
            }
            else if(rightEyeClosed && !leftEyeClosed)
            {
                emoji = Emoji.RIGHT_WINK;
            }
            else if(leftEyeClosed)
            {
                emoji = Emoji.CLOSED_EYE_SMILE;
            }
            else{
                emoji = Emoji.SMILE;
            }
        }
        else{
            if(leftEyeClosed && !rightEyeClosed)
            {
                emoji = Emoji.LEFT_WINK_FROWN;
            }
            else if(rightEyeClosed && !leftEyeClosed )
            {
                emoji = Emoji.RIGHT_WINK_FROWN;
            }
            else if(leftEyeClosed)
            {
                emoji = Emoji.CLOSED_EYE_FROWN;
            }
            else{
                emoji = Emoji.FROWN;
            }
        }
        Log.e(TAG, "getRightEmoji: "+emoji.name() );
        return emoji;
    }


    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face)
    {
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth()
                ,backgroundBitmap.getHeight(),
                backgroundBitmap.getConfig());

        //scale factor of the emoji
        float scaleFactor = EMOJI_SCALE_FACTOR;

        //new height and width for emoji
        int newEmojiHeight = (int) (face.getHeight() * scaleFactor);
        int newEmojiWidth  = (int) (face.getWidth() * scaleFactor);

        //scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap,newEmojiWidth,newEmojiHeight,false);

        //determine the position of the face
        float emojiPositionX = (face.getPosition().x + face.getWidth()/2) - (emojiBitmap.getWidth() /2);
        float emojiPositionY = (face.getPosition().y + face.getHeight()/2) - (emojiBitmap.getHeight() /3);

        //create a canvas to draw the bitmap
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap,0,0,null);
        canvas.drawBitmap(emojiBitmap,emojiPositionX,emojiPositionY,null);
        return resultBitmap;
    }

    private enum Emoji{
        SMILE,
        FROWN,
        LEFT_WINK,
        RIGHT_WINK,
        LEFT_WINK_FROWN,
        RIGHT_WINK_FROWN,
        CLOSED_EYE_SMILE,
        CLOSED_EYE_FROWN,
    }


}
