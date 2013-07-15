package ca.concordia.resolute.core.chat;

import gate.Document;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

public class ConsoleMessage implements MessageListener, ChatMessageListener{

	@Override
	public void processMessage(Chat chat, Message message) {
		if ((message != null) && (message.getBody() != null)) {
			System.out.println("You've got new message :");
			System.out.println(message.getBody());
		}
	}

	@Override
	public void newChatMessage(ConversationAPI conversation,
			ca.concordia.resolute.core.chat.Message msg) {
		System.out.println(msg.getId() + " -> " + msg.getMsg());
		Document doc = conversation.getDoc();
		if (doc != null){
			Object age = doc.getFeatures().get("Age");
			if (age != null && !age.toString().equals("-1"))
				System.out.println("System detect the age = " + age);
		}
	}

	@Override
	public void endChat() {
		
	}

}
