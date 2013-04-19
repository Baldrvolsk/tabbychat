package acs.tabbychat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.src.GuiChat;
import net.minecraft.src.GuiNewChat;
import net.minecraft.src.MathHelper;
import net.minecraft.src.ScaledResolution;
import net.minecraft.src.ChatLine;
import net.minecraft.src.StringTranslate;
import net.minecraft.src.StringUtils;
import net.minecraft.src.ChatClickData;

public class GuiNewChatTC extends GuiNewChat {

	public final Minecraft mc;
	public ScaledResolution sr; // change to protected later
	protected int chatWidth = 320;
	protected int chatHeight = 0;
	protected List<String> sentMessages = new ArrayList<String>();
	protected List<ChatLine> backupLines = new ArrayList<ChatLine>();
	protected List<ChatLine> chatLines = new ArrayList<ChatLine>();
	private int scrollOffset = 0;
	private boolean chatScrolled = false;
	public static final GuiNewChatTC me = new GuiNewChatTC(); 
	
	public GuiNewChatTC() {
		this(Minecraft.getMinecraft());		
	}
	
	public GuiNewChatTC(Minecraft par1Minecraft) {
		super(par1Minecraft);
		this.mc = par1Minecraft;
		this.sr = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
	}
	
	public @Override void drawChat(int currentTick) {
		if(this.mc.gameSettings.chatVisibility != 2) {
			this.sr = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
			this.chatHeight = 0;
			this.chatWidth = 320;
			int maxDisplayedLines = 0;
			boolean chatOpen = false;
			int validLinesDisplayed = 0;
			int numLinesTotal = this.chatLines.size();
			float chatOpacity = this.mc.gameSettings.chatOpacity * 0.9f + 0.1f;
			float chatScaling = this.func_96131_h();
			int timeStampOffset = 0;;
			int fadeTicks = 200;
			if (numLinesTotal > 0) {
				chatOpen = this.getChatOpen();
			}
			
			if(TabbyChat.instance.enabled()) {
				if(TabbyChat.instance.advancedSettings.customChatBoxSize.getValue()) {
					float scaleFactor;
					if(chatOpen)
						scaleFactor = TabbyChat.instance.advancedSettings.chatBoxFocHeight.getValue() / 100.0f;
					else
						scaleFactor = TabbyChat.instance.advancedSettings.chatBoxUnfocHeight.getValue() / 100.0f;
					maxDisplayedLines = (int)Math.floor((float)(this.sr.getScaledHeight() - 51) * scaleFactor / 9.0f);
				} else {
					maxDisplayedLines = this.func_96127_i();
					this.chatWidth = MathHelper.ceiling_float_int((float)this.func_96126_f() / chatScaling);
				}
				if(TabbyChat.instance.generalSettings.timeStampEnable.getValue())
					timeStampOffset = mc.fontRenderer.getStringWidth(((TimeStampEnum)TabbyChat.instance.generalSettings.timeStampStyle.getValue()).maxTime);
				if (TabbyChat.instance.advancedSettings.customChatBoxSize.getValue()) {
					int curWidth = this.sr.getScaledWidth() - 14 - timeStampOffset;
					float screenWidthScale = TabbyChat.instance.advancedSettings.chatBoxWidth.getValue() / 100.0f;
					this.chatWidth = MathHelper.ceiling_float_int(screenWidthScale * curWidth / chatScaling);
				}
				fadeTicks = TabbyChat.instance.advancedSettings.chatFadeTicks.getValue().intValue();
			} else {
				maxDisplayedLines = this.func_96127_i();
				this.chatWidth = MathHelper.ceiling_float_int((float)this.func_96126_f() / chatScaling);
			}
			
			GL11.glPushMatrix();
			GL11.glTranslatef(2.0f, 20.0f, 0.0f);
			GL11.glScalef(chatScaling, chatScaling, 1.0f);
			
			int lineCounter;
			int lineAge;
			int currentOpacity;
			int _size = this.chatLines.size();
			// Display valid chat lines
			for(lineCounter = 0; lineCounter + this.scrollOffset  < _size && lineCounter < maxDisplayedLines; ++lineCounter) {
				this.chatHeight = lineCounter * 9;
				ChatLine _line = this.chatLines.get(lineCounter + this.scrollOffset);
				if(_line == null) continue;
				lineAge = currentTick - _line.getUpdatedCounter(); 
				if(lineAge < fadeTicks || chatOpen) {
					double agePercent = (double)currentTick / (double)fadeTicks;
					agePercent = 10.0D * (1.0D - agePercent);
					agePercent = Math.min(0.0D, agePercent);
					agePercent = Math.max(1.0D, agePercent);
					agePercent *= agePercent;
					currentOpacity = (int)(255.0D * agePercent);
					if (chatOpen)
						currentOpacity = 255;
					currentOpacity = (int)((float)currentOpacity * chatOpacity);
					++validLinesDisplayed;
					if(currentOpacity > 3) {
						byte xOrigin = 3;
						int yOrigin = -lineCounter * 9;
						drawRect(xOrigin, yOrigin-9, xOrigin + this.chatWidth + 4 + timeStampOffset, yOrigin, currentOpacity / 2 << 24);
						GL11.glEnable(GL11.GL_BLEND);
						String _chat = _line.getChatLineString();
						if(!this.mc.gameSettings.chatColours)
							_chat = StringUtils.stripControlCodes(_chat);
						this.mc.fontRenderer.drawStringWithShadow(_chat, xOrigin, yOrigin-8, 0xffffff + (currentOpacity << 24));
					}
				}
			}
			// Draw the vanilla scroll bar
			if(chatOpen && !TabbyChat.instance.enabled()) {
				int fontHeight = this.mc.fontRenderer.FONT_HEIGHT;
				GL11.glTranslatef(-3.0F, 0.0F, 0.0F);
				int allLineHeight = numLinesTotal * fontHeight + numLinesTotal;
				int allValidHeight = validLinesDisplayed * fontHeight + validLinesDisplayed;
				if(allLineHeight != allValidHeight) {
					int scrollPos = this.scrollOffset * allValidHeight / numLinesTotal;
					int scrollMax = allValidHeight * allValidHeight / allLineHeight;
					int scrollOpacity = (scrollPos > 0) ? 170 : 96;
					int scrollBarColor = this.chatScrolled ? 0xCC3333 : 0x3333AA;
					drawRect(0, -scrollPos, 2, -scrollPos - scrollMax, scrollBarColor + (scrollOpacity << 24));
					drawRect(2, -scrollPos, 1, -scrollPos - scrollMax, 0xCCCCCC + (scrollOpacity << 24));
				}
			}
			GL11.glPopMatrix();
		}
	}

	public @Override void clearChatMessages() {
		this.chatLines.clear();
		this.backupLines.clear();
		this.sentMessages.clear();
	}
	
	public @Override void printChatMessage(String _msg) {
		this.printChatMessageWithOptionalDeletion(_msg, 0);
	}
	
	public @Override void printChatMessageWithOptionalDeletion(String _msg, int flag) {
		this.func_96129_a(_msg, flag, this.mc.ingameGUI.getUpdateCounter(), false);
		this.mc.getLogAgent().logInfo("[CHAT] " + _msg);
	}

	public void func_96129_a(String _msg, int id, int tick, boolean backupFlag) {
		boolean chatOpen = this.getChatOpen();
		boolean isLineOne = true;
		List<ChatLine> multiLineChat = new ArrayList<ChatLine>();
		// Delete message if requested
		if(id != 0)
			this.deleteChatLine(id);
		// Split message by available chatbox space
		int maxWidth = MathHelper.floor_float((float)this.func_96126_f() / this.func_96131_h());
		if(TabbyChat.instance.enabled()) {
			TabbyChat.instance.checkServer();
			if(TabbyChat.instance.advancedSettings.customChatBoxSize.getValue())
				maxWidth = this.chatWidth;
		}
		Iterator lineIter = this.mc.fontRenderer.listFormattedStringToWidth(_msg, maxWidth).iterator();

		// Prepare list of chatlines
		while(lineIter.hasNext()) {
			String _line = (String)lineIter.next();
			if(chatOpen && this.scrollOffset > 0) {
				this.chatScrolled = true;
				this.scroll(1);
			}
			if(!isLineOne) {
				_line = " " + _line;
			}
			multiLineChat.add(new ChatLine(tick, _line, id));
			isLineOne = false;
		}
		
		// Add chatlines to appropriate lists
		if(TabbyChat.instance.enabled()) {
			int ret = TabbyChat.instance.processChat(multiLineChat);
		} else {
			int _len = multiLineChat.size();
			for(int i=0; i<_len; i++) {
				this.chatLines.add(0, multiLineChat.get(i));
				if (!backupFlag)
					this.backupLines.add(0, multiLineChat.get(i));
			}
		}
		
		// Trim lists to size as needed
		int maxChats = TabbyChat.instance.enabled() ? Integer.parseInt(TabbyChat.instance.advancedSettings.chatScrollHistory.getValue()) : 100;
		int chatLineSize = this.chatLines.size();
		int cmdLineSize = this.backupLines.size();
		if(chatLineSize >= maxChats + 5)
			this.chatLines.subList(chatLineSize-11, chatLineSize-1).clear();
		if(!backupFlag) {
			if(cmdLineSize >= maxChats + 5)
				this.backupLines.subList(cmdLineSize-11, cmdLineSize-1).clear();
		}
		
	}

	public @Override void func_96132_b() {
		// Chat settings have changed
		this.chatLines.clear();
		this.resetScroll();
		
		for(int i=this.backupLines.size()-1; i>=0; --i) {
			ChatLine _cl = this.backupLines.get(i);
			this.func_96129_a(_cl.getChatLineString(), _cl.getChatLineID(), _cl.getUpdatedCounter(), true);
		}
	}

	public @Override List getSentMessages() {
		return this.sentMessages;
	}
	
	public @Override void addToSentMessages(String _msg) {
		if(this.sentMessages.isEmpty() || !(this.sentMessages.get(this.sentMessages.size()-1)).equals(_msg)) {
			this.sentMessages.add(_msg);
		}
	}

	public @Override void resetScroll() {
		this.scrollOffset = 0;
		this.chatScrolled = false;
	}
	
	public @Override void scroll(int _lines) {
		int maxLineDisplay;
		if(TabbyChat.instance.enabled() && TabbyChat.instance.advancedSettings.customChatBoxSize.getValue()) {
			float scaleFactor;
			if(this.getChatOpen())
				scaleFactor = TabbyChat.instance.advancedSettings.chatBoxFocHeight.getValue() / 100.0f;
			else
				scaleFactor = TabbyChat.instance.advancedSettings.chatBoxUnfocHeight.getValue() / 100.0f;
			maxLineDisplay = (int)Math.floor((float)(this.sr.getScaledHeight() - 51)*scaleFactor / 9.0f);
		} else
			maxLineDisplay = this.func_96127_i();
		
		this.scrollOffset += _lines;
		int numLines = this.chatLines.size();
		this.scrollOffset = Math.min(this.scrollOffset, numLines - maxLineDisplay);
		if(this.scrollOffset <= 0) {
			this.scrollOffset = 0;
			this.chatScrolled = false;
		}
	}

	public @Override ChatClickData func_73766_a(int clickX, int clickY) {
		if(!this.getChatOpen()) return null;
		else {
			ScaledResolution _sr = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
			int scaleFactor = _sr.getScaleFactor();
			float scaleSetting = this.func_96131_h();
			int clickXRel = clickX / scaleFactor - 3;
			int clickYRel = clickY / scaleFactor - 28;
			clickXRel = MathHelper.floor_float((float)clickXRel / scaleSetting);
			clickYRel = MathHelper.floor_float((float)clickYRel / scaleSetting);
			if(clickXRel >= 0 && clickYRel >= 0) {
				int displayedLines = Math.min(this.getHeightSetting() / 9, this.chatLines.size());
				if(clickXRel <= MathHelper.floor_float((float)this.chatWidth / scaleSetting)
						&& clickYRel < this.mc.fontRenderer.FONT_HEIGHT * displayedLines + displayedLines) {
					int lineIndex = clickYRel / this.mc.fontRenderer.FONT_HEIGHT + this.scrollOffset;
					if(lineIndex >= displayedLines + this.scrollOffset || this.chatLines.get(lineIndex) == null) return null;
					else return new ChatClickData(this.mc.fontRenderer, this.chatLines.get(lineIndex), clickXRel, clickYRel - (lineIndex - this.scrollOffset) * this.mc.fontRenderer.FONT_HEIGHT + lineIndex);
				} else return null;
			} else return null;
		}
	}

	public @Override void addTranslatedMessage(String par1Str, Object ... par2ArrayOfObj) {
		this.printChatMessage(StringTranslate.getInstance().translateKeyFormat(par1Str, par2ArrayOfObj));
	}
	
	public @Override boolean getChatOpen() {
		return (this.mc.currentScreen instanceof GuiChat || this.mc.currentScreen instanceof GuiChatTC);
	}
	
	public @Override void deleteChatLine(int _id) {
		Iterator _iter = this.chatLines.iterator();
		ChatLine _cl;
		do {
			if(!_iter.hasNext()) {
				_iter = this.backupLines.iterator();
				do {
					if(!_iter.hasNext()) {
						return;
					}
					_cl = (ChatLine)_iter.next();
				} while(_cl.getChatLineID() != _id);
				_iter.remove();
				return;
			}
			_cl = (ChatLine)_iter.next();
		} while(_cl.getChatLineID() != _id);
		_iter.remove();
	}
	
	public int getHeightSetting() {
		if (TabbyChat.instance.enabled() && TabbyChat.instance.advancedSettings.customChatBoxSize.getValue()) {
			float scaleFactor = TabbyChat.instance.advancedSettings.chatBoxFocHeight.getValue() / 100.0f;
			return (int)Math.floor((float)(this.sr.getScaledHeight() - 51) * scaleFactor);
		} else
			return func_96130_b(this.mc.gameSettings.chatHeightFocused);
	}

	public int getWidthSetting() {
		return this.chatWidth;
	}

	public float getScaleSetting() {
		return this.func_96131_h();
	}

	public int GetChatHeight() {
		return this.chatLines.size();
	}

	public void addChatLines(List _add) {
		this.chatLines.addAll(_add);
	}

	public void addChatLines(int _pos, List _add) {
		this.chatLines.addAll(_pos, _add);
	}

	public void setChatLines(int _pos, List<ChatLine> _add) {
		for (int i=0; i < _add.size(); i++)
			this.chatLines.set(_pos+i, _add.get(i));
	}

	public void clearChatLines() {
		this.resetScroll();
		this.chatLines.clear();
	}

	public int chatLinesTraveled() {
		return this.scrollOffset;
	}

	public void setVisChatLines(int _move) {
		this.scrollOffset = _move;
	}

	public int lastUpdate() {
		return ((ChatLine)this.chatLines.get(this.chatLines.size()-1)).getUpdatedCounter();
	}

	public void mergeChatLines(List<ChatLine> _new) {
		ArrayList<ChatLine> _current = (ArrayList<ChatLine>)this.chatLines;
		if (_new == null || _new.size() <= 0) return;

		int _c = 0;
		int _n = 0;
		int dt = 0;
		int max = _new.size();
		while (_n < max && _c < _current.size()) {
			dt = _new.get(_n).getUpdatedCounter() - _current.get(_c).getUpdatedCounter();
			if (dt > 0) {
				_current.add(_c, _new.get(_n));
				_n++;
			} else if (dt == 0) {
				if (_current.get(_c).equals(_new.get(_n))) {
					_c++;
					_n++;
				} else
					_c++;
			} else
				_c++;
		}

		while (_n < max) {
			_current.add(_current.size(), _new.get(_n));
			_n++;
		}
	}
}