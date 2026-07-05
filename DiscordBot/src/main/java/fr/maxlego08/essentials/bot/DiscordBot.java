package fr.maxlego08.essentials.bot;

import fr.maxlego08.essentials.bot.command.CommandManager;
import fr.maxlego08.essentials.bot.config.Configuration;
import fr.maxlego08.essentials.bot.config.ConfigurationManager;
import fr.maxlego08.essentials.bot.config.FeatureManager;
import fr.maxlego08.essentials.bot.link.LinkManager;
import fr.maxlego08.essentials.bot.listener.CommandListener;
import fr.maxlego08.essentials.bot.storage.StorageManager;
import fr.maxlego08.essentials.bot.sync.BoostListener;
import fr.maxlego08.essentials.bot.sync.RoleSyncService;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.Scanner;

public class DiscordBot {

    private final ConfigurationManager configurationManager;
    private final Configuration configuration;

    private final CommandManager commandManager;
    private final StorageManager storageManager;

    private LinkManager linkManager;
    private FeatureManager featureManager;

    // ✅ NEW: ROLE SYNC SERVICE
    private RoleSyncService roleSyncService;

    private JDA jda;
    private Scanner scanner;

    private DiscordBot() {

        // =========================
        // CONFIG LOAD
        // =========================
        this.configurationManager = new ConfigurationManager();
        this.configuration = new Configuration();
        this.configuration.loadConfiguration(configurationManager.getConfig());

        // =========================
        // FEATURE MANAGER
        // =========================
        this.featureManager = new FeatureManager(this.configuration);

        // =========================
        // MANAGERS
        // =========================
        this.commandManager = new CommandManager(this);

        this.storageManager = new StorageManager();
        this.storageManager.connect(this.configuration);

        this.linkManager = new LinkManager(this);

        // ✅ INIT ROLE SYNC SERVICE
        this.roleSyncService = new RoleSyncService(this);

        // =========================
        // JDA SETUP
        // =========================
        var builder = JDABuilder.createDefault(this.configuration.getBotToken())
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_EXPRESSIONS)
                .enableIntents(GatewayIntent.DIRECT_MESSAGES)
                .enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .enableIntents(GatewayIntent.GUILD_MESSAGES)
                .enableIntents(GatewayIntent.GUILD_MESSAGE_TYPING)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(new CommandListener(this))
                .addEventListeners(this.linkManager)
                .addEventListeners(new BoostListener(this));

        this.jda = builder.build();

        // =========================
        // SYSTEMS
        // =========================
        this.addShutdownHook();
        this.listenForCommands();
    }

    public static void main(String[] args) {
        new DiscordBot();
    }

    // =========================
    // SHUTDOWN HOOK
    // =========================
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (this.jda != null) {
                this.jda.shutdownNow();
                System.out.println("Bot disconnected by shutdown hook.");
            }
        }));
    }

    // =========================
    // CONSOLE STOP COMMAND
    // =========================
    private void listenForCommands() {
        this.scanner = new Scanner(System.in);

        Thread commandThread = new Thread(() -> {
            while (true) {
                String command = scanner.nextLine();

                if (command.equalsIgnoreCase("stop")) {
                    if (this.jda != null) {
                        this.jda.shutdownNow();
                        System.out.println("Bot stopped by user command.");
                    }
                    System.exit(0);
                    break;
                }
            }
            scanner.close();
        });

        commandThread.setDaemon(true);
        commandThread.start();
    }

    // =========================
    // GETTERS
    // =========================

    public Configuration getConfiguration() {
        return configuration;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public LinkManager getLinkManager() {
        return linkManager;
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public RoleSyncService getRoleSyncService() {
        return roleSyncService;
    }

    public JDA getJda() {
        return jda;
    }

    // =========================
    // RELOAD
    // =========================
    public void reload() {
        this.configurationManager.loadOrCreateConfig();
        this.configuration.loadConfiguration(configurationManager.getConfig());
        this.featureManager = new FeatureManager(this.configuration);

        // refresh role sync service too
        this.roleSyncService = new RoleSyncService(this);
    }
}