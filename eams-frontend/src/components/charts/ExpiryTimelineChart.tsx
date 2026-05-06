import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { useChartTheme } from './useChartTheme'

export interface TimelinePoint {
  month: string
  certs: number
  licences: number
}

export default function ExpiryTimelineChart({ data }: { data: TimelinePoint[] }) {
  const t = useChartTheme()
  return (
    <ResponsiveContainer width="100%" height={250}>
      <BarChart data={data} barGap={4}>
        <CartesianGrid strokeDasharray="3 3" stroke={t.grid} />
        <XAxis
          dataKey="month"
          tick={{ fill: t.tick, fontSize: 11 }}
          axisLine={{ stroke: t.grid }}
        />
        <YAxis
          tick={{ fill: t.tick, fontSize: 11 }}
          axisLine={{ stroke: t.grid }}
        />
        <Tooltip
          contentStyle={{ backgroundColor: t.tooltipBg, border: `1px solid ${t.grid}`, borderRadius: 3, color: t.tickStrong }}
          itemStyle={{ color: t.tickStrong }}
          labelStyle={{ color: t.tickStrong }}
        />
        <Bar dataKey="certs" name="Certificates" fill="#6B2D0E" radius={[2, 2, 0, 0]} />
        <Bar dataKey="licences" name="Licences" fill="#7A6B1A" radius={[2, 2, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  )
}
