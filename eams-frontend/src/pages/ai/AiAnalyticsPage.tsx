import { useEffect, useRef, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Bot, Send, User } from 'lucide-react'
import apiClient from '@/api/client'

interface ChatItem { role: 'user' | 'assistant'; message: string }

const suggestedQuestions = [
  'How many assets are registered?',
  'Show asset distribution by category',
  'Show asset distribution by status',
  'Which certificates are expiring soon?',
  'Which licences are expiring soon?',
  'Which licences are overallocated on seats?',
  'How many users are in the system?',
  'Give me an executive dashboard summary',
]

export default function AiAnalyticsPage() {
  const [messages, setMessages] = useState<ChatItem[]>([
    {
      role: 'assistant',
      message:
        "Hi, I'm the **E-AMS Data Assistant**. I answer only from your live database — no external AI APIs. Try one of the quick prompts, or ask your own question.",
    },
  ])
  const [input, setInput] = useState('')
  const scrollRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' })
  }, [messages])

  const chatMutation = useMutation({
    mutationFn: async (message: string) => {
      const r = await apiClient.post('/ai/chat', { message })
      return r.data.data as { message: string }
    },
    onSuccess: (data) => setMessages((prev) => [...prev, { role: 'assistant', message: data.message }]),
    onError: () =>
      setMessages((prev) => [...prev, { role: 'assistant', message: "I couldn't process that request right now." }]),
  })

  const ask = (question: string) => {
    setMessages((prev) => [...prev, { role: 'user', message: question }])
    setInput('')
    chatMutation.mutate(question)
  }

  return (
    <div className="page-content">
      <div className="page-header">
        <h1 className="page-title">AI Analytics</h1>
        <p className="page-subtitle">
          FR-AI · Data-grounded assistant for NATIONAL BANK OF RWANDA — queries run against your live E-AMS data.
        </p>
      </div>

      <div className="ai-layout">
        <aside className="card ai-sidebar">
          <h3 className="section-title">Quick Prompts</h3>
          <div className="ai-suggestions">
            {suggestedQuestions.map((q) => (
              <button key={q} className="ai-suggestion" onClick={() => ask(q)} disabled={chatMutation.isPending}>
                {q}
              </button>
            ))}
          </div>
        </aside>

        <section className="card ai-chat">
          <div ref={scrollRef} className="ai-messages">
            {messages.map((item, index) => (
              <div key={index} className={`ai-bubble ai-bubble-${item.role}`}>
                <div className="ai-bubble-avatar">{item.role === 'user' ? <User size={14} /> : <Bot size={14} />}</div>
                <div className="ai-bubble-text">{renderMarkdown(item.message)}</div>
              </div>
            ))}
            {chatMutation.isPending && (
              <div className="ai-bubble ai-bubble-assistant">
                <div className="ai-bubble-avatar"><Bot size={14} /></div>
                <div className="ai-bubble-text ai-typing"><span /><span /><span /></div>
              </div>
            )}
          </div>
          <form
            className="ai-composer"
            onSubmit={(e) => {
              e.preventDefault()
              const t = input.trim()
              if (t) ask(t)
            }}
          >
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Ask about assets, licences, certificates, users, or dashboard KPIs…"
            />
            <button type="submit" className="btn btn-primary" disabled={chatMutation.isPending || !input.trim()}>
              <Send size={16} /> Send
            </button>
          </form>
        </section>
      </div>
    </div>
  )
}

function renderMarkdown(text: string) {
  const lines = text.split('\n')
  return (
    <div>
      {lines.map((line, idx) => {
        const parts = line.split(/(\*\*[^*]+\*\*)/g)
        return (
          <p key={idx} className="ai-line">
            {parts.map((part, i) =>
              part.startsWith('**') && part.endsWith('**') ? (
                <strong key={i}>{part.slice(2, -2)}</strong>
              ) : part.startsWith('_') && part.endsWith('_') ? (
                <em key={i}>{part.slice(1, -1)}</em>
              ) : (
                <span key={i}>{part}</span>
              ),
            )}
          </p>
        )
      })}
    </div>
  )
}
