package com.starrycammod;


import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class camSpawnCommandReg
 {
     //Adds the command /coolCam and relevant arguments
     private camjsonWriter jsonList = new camjsonWriter();
     @SubscribeEvent // on the game event bus
     public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("coolCam").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .executes(
                                this::NEA_ERR
                                )
                        .then(Commands.argument("AddDelOrPlay" , StringArgumentType.string())
                        .executes(context ->
                                {
                                    String argEntered = StringArgumentType.getString(context, "AddDelOrPlay");
                                    return this.AddDelPlay(context, argEntered);
                                }
                        ).then(Commands.argument("Duration", IntegerArgumentType.integer())
                                        .executes( context ->
                                        {
                                            String argEntered = StringArgumentType.getString(context, "AddDelOrPlay");
                                            int durationArg = IntegerArgumentType.getInteger(context, "Duration");
                                            return this.PlayWithCamDur(context, argEntered, durationArg);
                                        })
                                )));
     }

     //originally just gave usage. Now prints camera list. and locations.
    public int NEA_ERR(CommandContext<CommandSourceStack> context)
     {
         context.getSource().sendSuccess(() -> Component.literal(jsonList.getCameras().toString()), true);
         return 0;
     }

     //resolves first argument and executes according logic.
     public int AddDelPlay(CommandContext<CommandSourceStack> context, String firstArg)
     {
         jsonList.readCamFile();
         if(!((firstArg.equals("Add")) || (firstArg.equals("Del")) || (firstArg.equals("Play"))))
         {
             return NEA_ERR(context);
         }
         switch (firstArg) {
             case "Add" -> {
                 context.getSource().sendSuccess(() -> Component.literal("Adding Camera"), true);
                 Vec3 camPos = context.getSource().getPosition();
                 float cameraLookingX = context.getSource().getPlayer().getXRot();
                 float cameraLookingY = context.getSource().getPlayer().getYRot();
                 jsonList.addCamera(new camWriter("Test Camera", camPos, cameraLookingX, cameraLookingY));
                 context.getSource().sendSuccess(() -> Component.literal(jsonList.getCameras().toString()), true);
                 jsonList.writeToCamFile();
                 return 1;
             }
             case "Del" -> {
                 jsonList.readCamFile();
                 context.getSource().sendSuccess(() -> Component.literal("Removing Camera"), true);
                 jsonList.removeCamera();
                 jsonList.writeToCamFile();
                 return 1;
             }
             case "Play" -> {
                 context.getSource().sendFailure(( Component.literal("Usage: /coolCam Play <Duration>")));
                 return 1;
             }
         }
         return 2;
     }

     //duration argument should be exclusively used with play argument.
     public int PlayWithCamDur(CommandContext<CommandSourceStack> context, String firstArg, int durationArg)
     {
         if(!(firstArg.equals("Play")))
         {
             context.getSource().sendFailure(Component.literal("Usage: /coolCam Play <Duration>"));
             return 0;
         }
         playCameraLogic.playCamLoop(jsonList.getCameras(), context, durationArg);
         context.getSource().sendSuccess(() -> Component.literal("Playing Sequence"), true);
         return 1;
     }
 }
