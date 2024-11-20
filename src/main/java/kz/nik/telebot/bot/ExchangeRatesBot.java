package kz.nik.telebot.bot;


import kz.nik.telebot.exception.ServiceException;
import kz.nik.telebot.service.ExchangeRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;


@Component
public class ExchangeRatesBot extends TelegramLongPollingBot {

    private static final Logger LOG = (Logger) LoggerFactory.getLogger(ExchangeRatesBot.class);

    private static final String START="/start";
    private static final String USD="/usd";
    private static final String EUR="/eur";
    private static final String HELP="/help";

    @Autowired
    private ExchangeRateService exchangeRateService;

    public ExchangeRatesBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(!update.hasMessage() || !update.getMessage().hasText()){
            return;
        }
        var message=update.getMessage().getText();
        var chatId=update.getMessage().getChatId();
        switch (message){
            case START -> {
            String userName = update.getMessage().getChat().getUserName();
            startCommand(chatId, userName);
            }
            case USD -> usdCommand(chatId);
            case EUR -> eurCommand(chatId);
            case HELP -> helpCommand(chatId);
            default -> unkownCommand(chatId);
        }
    }

    @Override
    public String getBotUsername() {
        return "asd_qwe_zxc_bot";
    }

    private  void  sendMessage(Long chatId,String text){
        var chatIdStr=String.valueOf(chatId);
        var sendMessage =new SendMessage(chatIdStr,text);
        try {
            execute(sendMessage);
        }catch (TelegramApiException e){
            LOG.error("Error sending message",e);
        }
    }

    private void startCommand(Long chatId,String userName){
        var text = """
                Добро пожаловать в бот, %s!
                Узнать курсы валют.
                /usd - курс доллара
                /eur - курс евро
                
                /help - справка
                """;
        var formattedText = String.format(text,userName);
        sendMessage(chatId,formattedText);
    }

    private void usdCommand(Long chatId){
        String formattedText;
        try{
            var usd = exchangeRateService.getUSDExchangeRate();
            var text = "Курс доллара на %s составляет %s рублей";
            formattedText=String.format(text, LocalDate.now(),usd);
        }catch (ServiceException e){
            LOG.error("Ошибка получения курса",e);
            formattedText="Не удалось получить курс";
        }
        sendMessage(chatId,formattedText);
    }

    private void eurCommand(Long chatId){
        String formattedText;
        try{
            var eur = exchangeRateService.getEURExchangeRate();
            var text = "Курс евро на %s составляет %s рублей";
            formattedText=String.format(text, LocalDate.now(),eur);
        }catch (ServiceException e){
            LOG.error("Ошибка получения курса",e);
            formattedText="Не удалось получить курс";
        }
        sendMessage(chatId,formattedText);
    }

    private void helpCommand(Long chatId){
        var text = """
                Справка
                
                Узнать курсы валют.
                /usd - курс доллара
                /eur - курс евро
                
                /help - справка
                """;
        sendMessage(chatId,text);
    }

    private void unkownCommand(Long chatId){
        var text = "Не удалось распознать команду";
        sendMessage(chatId,text);
    }
}
