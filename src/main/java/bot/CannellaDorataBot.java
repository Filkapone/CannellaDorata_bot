package bot;

import business.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.File;
import java.util.*;


public class CannellaDorataBot extends TelegramLongPollingBot {

    private final Map<Long, Integer> processingUsers = new HashMap<>();
    String name;
    String breed;
    String photoId;
    File photo;

    @Override
    public String getBotUsername() {
        return Admin.getBotName();
    }

    @Override
    public String getBotToken() {
        return Admin.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        Logger logger = new Logger();

        if (update.hasMessage() && update.getMessage().hasPhoto()){
            if(processingUsers.containsKey(update.getMessage().getChatId())){
                addKitten(update);
            }
            getPhotoId(update);}

        else if (update.hasMessage() && update.getMessage().hasText()){

            logger.log(update.getMessage().getChatId(), update.getMessage().getText());

            if (update.getMessage().getText().equals("/start")){welcomeText(update);}
            if (update.getMessage().getText().equals("/help")){help(update.getMessage().getChatId());}
            if (update.getMessage().getText().equals("/breeders")) {showBreeders(update, null, 0,0);}
            if (update.getMessage().getText().equals("/kittens")){showKittens(update, 0, 0);}

            if(processingUsers.containsKey(update.getMessage().getChatId())){
                addKitten(update);
            }

            if(update.getMessage().getText().startsWith("$") && Admin.isAdmin(String.valueOf(update.getMessage().getChatId()))){
                    //todo: some admin actions
                if(update.getMessage().getText().startsWith("$addKitten")){
                    processingUsers.put(update.getMessage().getChatId(), 0);
                    addKitten(update);
                }
            }


            if(Cattery.isOurCat(update.getMessage().getText())){sendCatInfo(update);}


        }

        else if(update.hasCallbackQuery()){
            String data = update.getCallbackQuery().getData();
            logger.log(update.getCallbackQuery().getMessage().getChatId(), data);
            if (data.startsWith("book")){sendContacts(update.getCallbackQuery().getMessage().getChatId(), Cattery.getKitten(data.split("%")[1]).get().getBreed());}

            if (data.startsWith("breed_BEN")){
                showBreeders(update, "Bengal", Integer.parseInt(data.split("%")[1]), 0);
            }
            if (data.startsWith("breed_CRX")){
                showBreeders(update, "Cornish Rex",Integer.parseInt(data.split("%")[1]), 0);
            }
            if(data.startsWith("next_cat")){
                String[] params = data.split("%");
                showBreeders(update, params[1], Integer.parseInt(params[2]), Integer.parseInt(params[3])+1);
            }
            if(data.startsWith("prev_cat")){
                String[] params = data.split("%");
                showBreeders(update, params[1], Integer.parseInt(params[2]), Integer.parseInt(params[3])-1);
            }
            if(data.startsWith("next_kitten")){
                String[] params = data.split("%");
                showKittens(update, Integer.parseInt(params[1]), Integer.parseInt(params[2])+1);
            }
            if(data.startsWith("prev_kitten")){
                String[] params = data.split("%");
                showKittens(update, Integer.parseInt(params[1]), Integer.parseInt(params[2])+1);
            }
            if (data.startsWith("add_kitten")){
                addKitten(update);
            }
        }
    }

    private String getPhotoId(Update update) {
        return Objects.requireNonNull(update.getMessage().getPhoto().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElseThrow().getFileId());
    }

    private void showBreeders(Update update, String breed, int messageId, Integer i){
        List<Cat> cats = Cattery.getBreedCats(breed);
        long chatId = (update.hasCallbackQuery())? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId();

        if(messageId == 0) {
            messageId = getMessageId(chatId);
        }
        if(breed == null || breed.equals("")) {
            showBreeds(chatId, messageId);
        }

        InlineKeyboardMarkup markup = setPrevNextCatMarkup(breed, messageId, i);

        EditMessageMedia editMessageMedia = new EditMessageMedia();
        editMessageMedia.setMedia(new InputMediaPhoto(cats.get(i).getPhotoId()));
        editMessageMedia.setChatId(String.valueOf(chatId));
        editMessageMedia.setMessageId(messageId);

        EditMessageCaption editMessageCaption = new EditMessageCaption();
        editMessageCaption.setChatId(String.valueOf(chatId));
        editMessageCaption.setCaption(cats.get(i).getDescription());
        editMessageCaption.setMessageId(messageId);

        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(markup);

        try {
            execute(editMessageMedia);
            execute(editMessageCaption);
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    private void showBreeds(long chatId, int messageId){
        String BENcallback = "breed_BEN";
        String CRXcallvack = "breed_CRX";

        InlineKeyboardButton bengalButton = new InlineKeyboardButton();
        bengalButton.setText("Бенгальские");
        bengalButton.setCallbackData(BENcallback + "%" + messageId);
        InlineKeyboardButton cornishRexButton = new InlineKeyboardButton();
        cornishRexButton.setText("Корниш Рекс");
        cornishRexButton.setCallbackData(CRXcallvack + "%" + messageId);
        List<InlineKeyboardButton> buttonRow = new ArrayList<>();
        buttonRow.add(bengalButton);
        buttonRow.add(cornishRexButton);
        List<List<InlineKeyboardButton>> rowsList = new ArrayList<>();
        rowsList.add(buttonRow);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rowsList);

        EditMessageMedia photo = new EditMessageMedia();
        photo.setChatId(String.valueOf(chatId));
        photo.setMessageId(messageId);
        photo.setMedia(new InputMediaPhoto("AgACAgIAAxkBAAICy2KWW_fPp_vKb-IWwBc5MAbXH602AAL1vTEbcqywSJAoTxt9g8AuAQADAgADdwADJAQ"));

        EditMessageCaption caption = new EditMessageCaption();
        caption.setChatId(String.valueOf(chatId));
        caption.setMessageId(messageId);
        caption.setCaption("Нащ питомник занимается разведением кошек двух пород: Бенгальских и Корниш Рекс.\nКакая интересует Вас?");

        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(markup);

        try {
            execute(photo);
            execute(caption);
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendCatInfo(Update update){
        Cat cat = Cattery.getCat(update.getMessage().getText()).get();
        SendPhoto message = new SendPhoto();
        message.setChatId(String.valueOf(update.getMessage().getChatId()));
        message.setPhoto(new InputFile(cat.getPhotoId()));
        message.setCaption(cat.getDescription());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void welcomeText(Update update){
        String inStock = "<b>Имеются готовые к переезду котята!</b> /kittens";

        SendMessage msg = new SendMessage();
        msg.setParseMode("html");
        msg.setChatId(String.valueOf(update.getMessage().getChatId()));
        msg.setText("""
                \uD83D\uDC4B\uD83C\uDFFBДобро пожаловать в питомник корниш-рексов и бенгалов!
                У нас вы найдёте высокопородных, воспитанных и здоровых котят и кошек.
                
                Для знакомления с функциями бота нажмите: /help 
                
                
                """ + (Cattery.getAvailableKittens().isEmpty()? "" : inStock));

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void unavailable(Update update){
        SendMessage msg = new SendMessage();
        msg.setText("***Work In Progress***");
        msg.setChatId(String.valueOf(update.getMessage().getChatId()));

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private int getMessageId(long chatId){
        SendPhoto placeHolder = new SendPhoto();
        placeHolder.setChatId(String.valueOf(chatId));
        placeHolder.setPhoto(new InputFile("AgACAgIAAxkBAAIC02KWYTqZelGQy2gv_0LXwDYPEg3dAAIIvjEbcqywSC36QTsTChhfAQADAgADbQADJAQ"));

        try {
            return execute(placeHolder).getMessageId();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private InlineKeyboardMarkup setPrevNextCatMarkup(String breed, int messageId, int i){
        InlineKeyboardButton prevButton = new InlineKeyboardButton();
        prevButton.setText("<-");
        prevButton.setCallbackData("prev_cat%" + breed + "%" + messageId + "%" + i);
        InlineKeyboardButton nextButton = new InlineKeyboardButton();
        nextButton.setText("->");
        nextButton.setCallbackData("next_cat%" + breed + "%" + messageId + "%" + i);
        InlineKeyboardButton backToBreeds = new InlineKeyboardButton();
        backToBreeds.setText("Вернуться к породам");
        backToBreeds.setCallbackData("next_cat%" + "" + "%" + messageId + "%" + -1);
        List<InlineKeyboardButton> arrowsRow= new ArrayList<>();
        arrowsRow.add(prevButton);
        arrowsRow.add(nextButton);
        List<InlineKeyboardButton> backToBreedsRow = new ArrayList<>();
        backToBreedsRow.add(backToBreeds);
        List<List<InlineKeyboardButton>> rowsList = new ArrayList<>();
        rowsList.add(arrowsRow);
        rowsList.add(backToBreedsRow);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rowsList);
        return markup;
    }

    private InlineKeyboardMarkup setPrevNextKittenMarkup(Kitten kitten, int messageId, int i, int size){
        InlineKeyboardButton backToBreeds = new InlineKeyboardButton();
        backToBreeds.setText("Забронировать");
        backToBreeds.setCallbackData("book%" + kitten.getName());


        List<InlineKeyboardButton> backToBreedsRow = new ArrayList<>();
        backToBreedsRow.add(backToBreeds);
        List<List<InlineKeyboardButton>> rowsList = new ArrayList<>();

        if(size>1) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton();
            prevButton.setText("<-");
            prevButton.setCallbackData("prev_kitten%" + messageId + "%" + i + "%" + size);

            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText("->");
            nextButton.setCallbackData("next_kitten%" + +messageId + "%" + i + "%" + size);

            List<InlineKeyboardButton> arrowsRow= new ArrayList<>();
            arrowsRow.add(prevButton);
            arrowsRow.add(nextButton);
            rowsList.add(arrowsRow);
        }
        rowsList.add(backToBreedsRow);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rowsList);
        return markup;
    }

    private void sendContacts(Long chatId, String breed){
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setDisableWebPagePreview(true);
        msg.setParseMode("html");
        if(breed.equals("Bengal")){
            msg.setText(Admin.getBengalContacts());
        }
        if(breed.equals("Cornish Rex")) {
            msg.setText(Admin.getCornishContacts());
        }
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void help(long chatId){
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setParseMode("html");
        msg.setText("""
                Список доступных команд:
                
                /start - начать общение с ботом с начала.
                /help - вывести список доступных команд.
                /kittens - доступные к брони или даже перезду котята.
                /breeders - производители нашего питомника
                """);

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void showKittens(Update update, int messageId, Integer i){
        List<Kitten> kittens = Cattery.getAvailableKittens();
        long chatId = (update.hasCallbackQuery())? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId();

        if(messageId == 0) {
            messageId = getMessageId(chatId);
        }

        InlineKeyboardMarkup markup = setPrevNextKittenMarkup(kittens.get(i), messageId, i, kittens.size());

        EditMessageMedia editMessageMedia = new EditMessageMedia();
        editMessageMedia.setMedia(new InputMediaPhoto(kittens.get(i).getPhotoId()));
        editMessageMedia.setChatId(String.valueOf(chatId));
        editMessageMedia.setMessageId(messageId);

        EditMessageCaption editMessageCaption = new EditMessageCaption();
        editMessageCaption.setParseMode("html");
        editMessageCaption.setChatId(String.valueOf(chatId));
        editMessageCaption.setCaption(kittens.get(i).toString());
        editMessageCaption.setMessageId(messageId);

        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(String.valueOf(chatId));
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(markup);

        try {
            execute(editMessageMedia);
            execute(editMessageCaption);
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void addKitten(Update update){
        long chatId = (update.hasCallbackQuery())? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId();

        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));

        switch (processingUsers.get(chatId)) {
            case 0 -> {
                msg.setText("Введите имя Котенка:");
            }
            case 1 -> {
                name = update.getMessage().getText();

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<InlineKeyboardButton> buttons = new ArrayList<>();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();

                InlineKeyboardButton bengalButton = new InlineKeyboardButton();
                bengalButton.setText("Бенгальская");
                bengalButton.setCallbackData("add_kitten%BEN");
                buttons.add(bengalButton);

                InlineKeyboardButton cornishButton = new InlineKeyboardButton();
                cornishButton.setText("Корниш Рекс");
                cornishButton.setCallbackData("add_kitten%CRX");
                buttons.add(cornishButton);

                rows.add(buttons);
                markup.setKeyboard(rows);

                msg.setText("Выберите породу:");
                msg.setReplyMarkup(markup);
            }
            case 2 -> {
                String param = update.getCallbackQuery().getData().split("%")[1];
                if(param.equals("BEN")){
                    breed = "Bengal";
                }
                if(param.equals("CRX")){
                    breed = "Cornish Rex";
                }

                msg.setText("Отправьте фото:");
            }
            case 3 -> {
                photoId = getPhotoId(update);
                GetFile getFile = new GetFile();
                getFile.setFileId(photoId);
                String filePath = "";
                try {
                    filePath = execute(getFile).getFilePath();
                    photo = downloadFile(filePath);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                Cattery.addKitten(name, breed, photoId, photo);
                msg.setText("Готово!");
                try {
                    execute(msg);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                processingUsers.remove(update.getMessage().getChatId());
                name = null;
                breed = null;
                photoId = null;
                photo = null;
                return;
            }

        }
        try {
            execute(msg);
            processingUsers.replace(chatId, processingUsers.get(chatId)+1);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
