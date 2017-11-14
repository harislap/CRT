package rrdl.crt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.bassaer.chatmessageview.model.User;
import com.github.bassaer.chatmessageview.models.Message;
import com.github.bassaer.chatmessageview.views.ChatView;
import com.github.bassaer.chatmessageview.views.MessageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class Donation extends Fragment {

    private ChatView mChatView;
    private MessageList mMessageList;
    private ArrayList<User> mUsers;

    private int mReplyDelay = -1;

    private static final int READ_REQUEST_CODE = 100;




    public Donation() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static Donation newInstance() {
        return new Donation();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_donation, container, false);
        initUsers();
        mChatView = (ChatView) v.findViewById(R.id.chat_view);
        loadMessages();


        //User id
        int myId = 0;
        //User icon
        Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_2);
        //User name
        String myName = "Michael";
        final User me = new User(myId, myName, myIcon);

        //Set UI parameters if you need
        mChatView.setRightBubbleColor(Color.RED);
        mChatView.setLeftBubbleColor(Color.RED);
        mChatView.setBackgroundColor(Color.WHITE);
        mChatView.setSendButtonColor(ContextCompat.getColor(v.getContext(), R.color.bleeding));
        mChatView.setSendIcon(R.drawable.ic_action_send);
        mChatView.setRightMessageTextColor(Color.WHITE);
        mChatView.setLeftMessageTextColor(Color.WHITE);
        mChatView.setUsernameTextColor(Color.RED);
        mChatView.setSendTimeTextColor(Color.RED);
        mChatView.setDateSeparatorColor(Color.WHITE);
        mChatView.setInputTextHint("...");
        mChatView.setMessageMarginTop(5);
        mChatView.setMessageMarginBottom(5);


        //Click Send Button
        mChatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new message
                Message message = new Message.Builder()
                        .setUser(me)
                        .setRightMessage(true)
                        .setMessageText(mChatView.getInputText())
                        .hideIcon(true)
                        .build();
                //Set to chat view
                mChatView.send(message);
                mMessageList.add(message);
                //Reset edit text
                mChatView.setInputText("");

            }

        });
        return v;
    }

    private void receiveMessage(String sendText) {
        //Ignore hey
        if (!sendText.contains("hey")) {

            //Receive message
            final Message receivedMessage = new Message.Builder()
                    .setUser(mUsers.get(1))
                    .setRightMessage(false)
                    .setMessageText(sendText)
                    .setMessageStatusType(Message.MESSAGE_STATUS_ICON)
                    .build();

            if (sendText.equals(Message.Type.PICTURE.name())) {
                receivedMessage.setMessageText("Nice!");
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != READ_REQUEST_CODE || resultCode != RESULT_OK || data == null) {
            return;
        }
        Uri uri = data.getData();
        try {
            Bitmap picture = MediaStore.Images.Media.getBitmap(mChatView.getContext().getContentResolver(), uri);
            Message message = new Message.Builder()
                    .setRightMessage(true)
                    .setMessageText(Message.Type.PICTURE.name())
                    .setUser(mUsers.get(0))
                    .hideIcon(true)
                    .setPicture(picture)
                    .setType(Message.Type.PICTURE)
                    .setMessageStatusType(Message.MESSAGE_STATUS_ICON)
                    .build();
            mChatView.send(message);
            //Add message list
            mMessageList.add(message);
            receiveMessage(Message.Type.PICTURE.name());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();}



    private void initUsers() {
        mUsers = new ArrayList<>();
        //User id
        int myId = 0;
        //User icon
        Bitmap myIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_2);
        //User name
        String myName = "Michael";

        int yourId = 1;
        Bitmap yourIcon = BitmapFactory.decodeResource(getResources(), R.drawable.face_1);
        String yourName = "Emily";

        final User me = new User(myId, myName, myIcon);
        final User you = new User(yourId, yourName, yourIcon);

        mUsers.add(me);
        mUsers.add(you);
    }

    /**
     * Load saved messages
     */
    private void loadMessages() {
        List<Message> messages = new ArrayList<>();
        mMessageList = AppData.getMessageList(getContext());
        if (mMessageList == null) {
            mMessageList = new MessageList();
        } else {
            for (int i = 0; i < mMessageList.size(); i++) {
                Message message = mMessageList.get(i);
                //Set extra info because they were removed before save messages.
                for (User user : mUsers) {
                    if (message.getUser().getId() == user.getId()) {
                        message.getUser().setIcon(user.getIcon());
                    }
                }
                if (!message.isDateCell() && message.isRightMessage()) {
                    message.hideIcon(true);

                }
                message.setMessageStatusType(Message.MESSAGE_STATUS_ICON_RIGHT_ONLY);
                message.setStatusIconFormatter(new MyMessageStatusFormatter(getContext()));
                message.setStatus(MyMessageStatusFormatter.STATUS_DELIVERED);
                messages.add(message);
            }
        }
        MessageView messageView = mChatView.getMessageView();
        messageView.init(messages);
        messageView.setSelection(messageView.getCount() - 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        initUsers();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Save message
        AppData.putMessageList(getContext(), mMessageList);
    }

    @VisibleForTesting
    public ArrayList<User> getUsers() {
        return mUsers;
    }




}
