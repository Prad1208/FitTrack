import React, { useState, useEffect, useRef } from 'react';
import api from '../api/client';
import { 
  Bot, 
  Send, 
  X, 
  Sparkles, 
  Trash2, 
  ChevronDown, 
  MessageSquare, 
  Dumbbell,
  Apple,
  Zap,
  Minimize2
} from 'lucide-react';

const INITIAL_MESSAGE = {
  id: 'init-1',
  sender: 'ai',
  text: "Hey! I'm your FitTrack AI Coach. I can help you with exercise form, Indian high-protein diet tips, shoulder/knee pain substitutions, workout routines, or breaking plateaus. What are you working on today?",
  time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
};

const QUICK_PROMPTS = [
  { icon: Apple, label: 'High Protein Indian Veg', prompt: 'How to hit 100g protein on an Indian vegetarian diet?' },
  { icon: Dumbbell, label: 'Shoulder Pain Alternative', prompt: 'My shoulder hurts during bench press, what exercises can I substitute?' },
  { icon: Zap, label: 'Break Fat Loss Plateau', prompt: 'I have hit a weight loss plateau, what should I adjust?' },
  { icon: Dumbbell, label: 'Knee Pain Squat Fix', prompt: 'Best exercises to replace squats if my knees hurt?' }
];

export default function ChatbotWidget() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState(() => {
    try {
      const saved = sessionStorage.getItem('fittrack_chat_messages');
      return saved ? JSON.parse(saved) : [INITIAL_MESSAGE];
    } catch {
      return [INITIAL_MESSAGE];
    }
  });
  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);

  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  // Sync to session storage so switching tabs doesn't wipe conversation
  useEffect(() => {
    try {
      sessionStorage.setItem('fittrack_chat_messages', JSON.stringify(messages));
    } catch (e) {
      console.warn('Could not save chat messages to sessionStorage', e);
    }
  }, [messages]);

  // Scroll to bottom on new messages
  useEffect(() => {
    if (isOpen) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
      setUnreadCount(0);
      inputRef.current?.focus();
    }
  }, [messages, isOpen]);

  const handleSendMessage = async (msgText) => {
    const textToSend = typeof msgText === 'string' ? msgText : inputMessage;
    if (!textToSend || !textToSend.trim() || loading) return;

    const userMsg = {
      id: Date.now() + '-user',
      sender: 'user',
      text: textToSend.trim(),
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputMessage('');
    setLoading(true);

    try {
      const res = await api.post('/ai/assistant-chat', { message: textToSend.trim() });
      const aiReply = res.data?.response || "I'm ready to help with your training and nutrition!";
      
      const botMsg = {
        id: Date.now() + '-ai',
        sender: 'ai',
        text: aiReply,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };

      setMessages((prev) => [...prev, botMsg]);
      if (!isOpen) {
        setUnreadCount((c) => c + 1);
      }
    } catch (err) {
      console.error('Chatbot assistant error:', err);
      const errorMsg = {
        id: Date.now() + '-ai-err',
        sender: 'ai',
        text: 'Sorry, I had trouble connecting to the FitTrack AI server. Please make sure the backend is running and try again!',
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const clearChat = () => {
    const fresh = [INITIAL_MESSAGE];
    setMessages(fresh);
    sessionStorage.removeItem('fittrack_chat_messages');
  };

  // Helper to format bot markdown text cleanly
  const formatBotText = (text) => {
    const lines = text.split('\n');
    return lines.map((line, idx) => {
      // Bold syntax regex **bold text**
      const parts = line.split(/(\*\*.*?\*\*)/g);
      const renderedLine = parts.map((part, pIdx) => {
        if (part.startsWith('**') && part.endsWith('**')) {
          return <strong key={pIdx} className="font-semibold text-emerald-400">{part.slice(2, -2)}</strong>;
        }
        return part;
      });

      // Bullet point
      if (line.trim().startsWith('- ') || line.trim().startsWith('* ') || line.trim().startsWith('• ')) {
        return (
          <li key={idx} className="ml-4 list-disc my-1 text-slate-200">
            {renderedLine}
          </li>
        );
      }

      // Empty line / paragraph break
      if (!line.trim()) {
        return <div key={idx} className="h-2" />;
      }

      return (
        <p key={idx} className="my-1 text-slate-200 leading-relaxed">
          {renderedLine}
        </p>
      );
    });
  };

  return (
    <>
      {/* Floating Trigger Button */}
      <div className="fixed bottom-6 right-6 z-50 flex items-center">
        {!isOpen && (
          <button
            onClick={() => setIsOpen(true)}
            className="group relative flex items-center gap-2.5 px-4 py-3 bg-gradient-to-r from-brand-600 via-emerald-600 to-teal-600 hover:from-brand-500 hover:to-teal-500 text-white font-medium rounded-full shadow-lg shadow-emerald-950/40 hover:shadow-emerald-500/30 hover:scale-105 active:scale-95 transition-all duration-200"
            aria-label="Open FitTrack AI Chatbot"
          >
            {/* Animated Ping Ring */}
            <span className="absolute -top-1 -right-1 flex h-3.5 w-3.5">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-3.5 w-3.5 bg-emerald-500 border-2 border-slate-950"></span>
            </span>

            <div className="w-6 h-6 rounded-full bg-white/20 flex items-center justify-center">
              <Bot className="w-4 h-4 text-white" />
            </div>
            <span className="text-sm font-semibold tracking-wide hidden sm:inline">AI Assistant</span>
            <Sparkles className="w-3.5 h-3.5 text-amber-300 animate-pulse" />

            {unreadCount > 0 && (
              <span className="ml-1 px-1.5 py-0.5 text-[10px] font-bold bg-amber-500 text-slate-950 rounded-full">
                {unreadCount}
              </span>
            )}
          </button>
        )}
      </div>

      {/* Chatbot Window / Drawer */}
      {isOpen && (
        <div className="fixed bottom-5 right-5 z-50 w-[94vw] sm:w-[420px] h-[580px] max-h-[85vh] flex flex-col bg-slate-900/95 backdrop-blur-xl border border-slate-700/80 rounded-2xl shadow-2xl shadow-black/60 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
          
          {/* Header */}
          <div className="px-4 py-3.5 bg-slate-800/90 border-b border-slate-700/80 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="relative">
                <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-brand-600 to-teal-400 flex items-center justify-center text-white shadow-md shadow-brand-500/20">
                  <Bot className="w-5 h-5" />
                </div>
                <span className="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 bg-emerald-500 border-2 border-slate-900 rounded-full"></span>
              </div>
              <div>
                <div className="flex items-center gap-1.5">
                  <h3 className="text-sm font-bold text-white tracking-tight">FitTrack AI Coach</h3>
                  <span className="px-1.5 py-0.5 text-[9px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 rounded">
                    LIVE
                  </span>
                </div>
                <p className="text-[11px] text-slate-400">Context-aware fitness & nutrition assistant</p>
              </div>
            </div>

            {/* Header Controls */}
            <div className="flex items-center gap-1">
              <button
                onClick={clearChat}
                title="Clear Conversation"
                className="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-slate-700/60 rounded-lg transition-colors"
              >
                <Trash2 className="w-4 h-4" />
              </button>
              <button
                onClick={() => setIsOpen(false)}
                title="Minimize Chat"
                className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-700/60 rounded-lg transition-colors"
              >
                <ChevronDown className="w-5 h-5" />
              </button>
            </div>
          </div>

          {/* Quick Prompts Bar */}
          <div className="px-3 py-2 bg-slate-950/60 border-b border-slate-800/80 overflow-x-auto scrollbar-none flex items-center gap-2">
            {QUICK_PROMPTS.map((qp, i) => {
              const Icon = qp.icon;
              return (
                <button
                  key={i}
                  onClick={() => handleSendMessage(qp.prompt)}
                  disabled={loading}
                  className="flex items-center gap-1.5 px-2.5 py-1 text-[11px] whitespace-nowrap bg-slate-800 hover:bg-slate-700 hover:border-emerald-500/40 text-slate-300 hover:text-white border border-slate-700/60 rounded-full transition-all disabled:opacity-50"
                >
                  <Icon className="w-3 h-3 text-emerald-400" />
                  <span>{qp.label}</span>
                </button>
              );
            })}
          </div>

          {/* Messages Container */}
          <div className="flex-1 p-4 overflow-y-auto space-y-3.5 bg-slate-950/40 text-xs">
            {messages.map((msg) => {
              const isAi = msg.sender === 'ai';
              return (
                <div
                  key={msg.id}
                  className={`flex gap-2.5 ${isAi ? 'justify-start' : 'justify-end'}`}
                >
                  {isAi && (
                    <div className="w-7 h-7 rounded-lg bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400 shrink-0 mt-0.5">
                      <Bot className="w-4 h-4" />
                    </div>
                  )}

                  <div className={`max-w-[82%] flex flex-col ${isAi ? 'items-start' : 'items-end'}`}>
                    <div
                      className={`px-3.5 py-2.5 rounded-2xl shadow-sm text-xs leading-relaxed ${
                        isAi
                          ? 'bg-slate-800/90 text-slate-200 border border-slate-700/70 rounded-tl-none'
                          : 'bg-gradient-to-r from-brand-600 to-emerald-600 text-white rounded-tr-none'
                      }`}
                    >
                      {isAi ? formatBotText(msg.text) : msg.text}
                    </div>
                    <span className="text-[10px] text-slate-500 mt-1 px-1">
                      {msg.time}
                    </span>
                  </div>
                </div>
              );
            })}

            {/* Typing Indicator */}
            {loading && (
              <div className="flex gap-2.5 justify-start">
                <div className="w-7 h-7 rounded-lg bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400 shrink-0 mt-0.5">
                  <Bot className="w-4 h-4" />
                </div>
                <div className="bg-slate-800/90 border border-slate-700/70 rounded-2xl rounded-tl-none px-4 py-3 flex items-center gap-1.5 text-slate-400">
                  <span className="text-[11px] text-slate-400 mr-1">FitTrack AI is thinking</span>
                  <div className="w-1.5 h-1.5 bg-emerald-400 rounded-full animate-bounce [animation-delay:-0.3s]"></div>
                  <div className="w-1.5 h-1.5 bg-emerald-400 rounded-full animate-bounce [animation-delay:-0.15s]"></div>
                  <div className="w-1.5 h-1.5 bg-emerald-400 rounded-full animate-bounce"></div>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Input Bar */}
          <div className="p-3 bg-slate-900 border-t border-slate-800/90">
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSendMessage();
              }}
              className="flex items-center gap-2"
            >
              <input
                ref={inputRef}
                type="text"
                value={inputMessage}
                onChange={(e) => setInputMessage(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Ask about exercises, Indian food, macros..."
                disabled={loading}
                className="flex-1 bg-slate-950 border border-slate-700/80 focus:border-brand-500 rounded-xl px-3.5 py-2.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:ring-1 focus:ring-brand-500 transition-colors disabled:opacity-50"
              />
              <button
                type="submit"
                disabled={!inputMessage.trim() || loading}
                className="p-2.5 bg-gradient-to-r from-brand-600 to-emerald-600 hover:from-brand-500 hover:to-emerald-500 text-white rounded-xl shadow-md disabled:opacity-40 disabled:cursor-not-allowed transition-all"
                title="Send Message"
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
            <div className="mt-2 flex items-center justify-between text-[10px] text-slate-500 px-1">
              <span>Personalized with your daily targets</span>
              <span className="text-emerald-400 flex items-center gap-1">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
                AI Active
              </span>
            </div>
          </div>

        </div>
      )}
    </>
  );
}
