import os

telemetry_snippets = {
    "values-es": """
    <string name="usage_telemetry_title">ESTADÍSTICAS Y USO DE LA APP</string>
    <string name="usage_sessions">Sesiones: %1$d</string>
    <string name="usage_active_time">Tiempo activo: %1$d min</string>
    <string name="usage_interactions">Interacciones: %1$d</string>
""",
    "values-fr": """
    <string name="usage_telemetry_title">STATISTIQUES D\'UTILISATION DE L\'APPLICATION</string>
    <string name="usage_sessions">Sessions : %1$d</string>
    <string name="usage_active_time">Temps actif : %1$d min</string>
    <string name="usage_interactions">Interactions : %1$d</string>
""",
    "values-de": """
    <string name="usage_telemetry_title">APP-NUTZUNGSSTATISTIK</string>
    <string name="usage_sessions">Sitzungen: %1$d</string>
    <string name="usage_active_time">Aktive Zeit: %1$d Min.</string>
    <string name="usage_interactions">Interaktionen: %1$d</string>
""",
    "values-it": """
    <string name="usage_telemetry_title">STATISTICHE D\'USO DELL\'APP</string>
    <string name="usage_sessions">Sessioni: %1$d</string>
    <string name="usage_active_time">Tempo attivo: %1$d min</string>
    <string name="usage_interactions">Interazioni: %1$d</string>
""",
    "values-pt": """
    <string name="usage_telemetry_title">ESTATÍSTICAS DE USO DO APP</string>
    <string name="usage_sessions">Sessões: %1$d</string>
    <string name="usage_active_time">Tempo ativo: %1$d min</string>
    <string name="usage_interactions">Interações: %1$d</string>
""",
    "values-ru": """
    <string name="usage_telemetry_title">СТАТИСТИКА ИСПОЛЬЗОВАНИЯ ПРИЛОЖЕНИЯ</string>
    <string name="usage_sessions">Сессии: %1$d</string>
    <string name="usage_active_time">Активное время: %1$d мин</string>
    <string name="usage_interactions">Взаимодействия: %1$d</string>
""",
    "values-ja": """
    <string name="usage_telemetry_title">アプリ利用統計＆ステータス</string>
    <string name="usage_sessions">起動回数: %1$d回</string>
    <string name="usage_active_time">利用時間: %1$d分</string>
    <string name="usage_interactions">操作回数: %1$d回</string>
""",
    "values-ko": """
    <string name="usage_telemetry_title">앱 사용 통계 및 상태</string>
    <string name="usage_sessions">세션 수: %1$d회</string>
    <string name="usage_active_time">사용 시간: %1$d분</string>
    <string name="usage_interactions">상호작용: %1$d회</string>
""",
    "values-zh-rCN": """
    <string name="usage_telemetry_title">应用使用统计与数据</string>
    <string name="usage_sessions">会话次数: %1$d</string>
    <string name="usage_active_time">活跃时间: %1$d 分钟</string>
    <string name="usage_interactions">交互次数: %1$d</string>
""",
    "values-zh-rTW": """
    <string name="usage_telemetry_title">應用程式使用統計與數據</string>
    <string name="usage_sessions">會話次數: %1$d</string>
    <string name="usage_active_time">活躍時間: %1$d 分鐘</string>
    <string name="usage_interactions">互動次數: %1$d</string>
""",
    "values-hi": """
    <string name="usage_telemetry_title">ऐप उपयोग आँकड़े</string>
    <string name="usage_sessions">सत्र: %1$d</string>
    <string name="usage_active_time">सक्रिय समय: %1$d मिनट</string>
    <string name="usage_interactions">इंटरैक्शन: %1$d</string>
""",
    "values-ar": """
    <string name="usage_telemetry_title">إحصائيات استخدام التطبيق</string>
    <string name="usage_sessions">الجلسات: %1$d</string>
    <string name="usage_active_time">الوقت النشط: %1$d دقيقة</string>
    <string name="usage_interactions">التفاعلات: %1$d</string>
""",
    "values-vi": """
    <string name="usage_telemetry_title">THỐNG KÊ SỬ DỤNG ỨNG DỤNG</string>
    <string name="usage_sessions">Phiên: %1$d</string>
    <string name="usage_active_time">Thời gian hoạt động: %1$d phút</string>
    <string name="usage_interactions">Tương tác: %1$d</string>
""",
    "values-id": """
    <string name="usage_telemetry_title">STATISTIK PENGGUNAAN APLIKASI</string>
    <string name="usage_sessions">Sesi: %1$d</string>
    <string name="usage_active_time">Waktu Aktif: %1$d mnt</string>
    <string name="usage_interactions">Interaksi: %1$d</string>
""",
    "values-tr": """
    <string name="usage_telemetry_title">UYGULAMA KULLANIM İSTATİSTİKLERİ</string>
    <string name="usage_sessions">Oturumlar: %1$d</string>
    <string name="usage_active_time">Aktif Süre: %1$d dk</string>
    <string name="usage_interactions">Etkileşimler: %1$d</string>
""",
    "values-th": """
    <string name="usage_telemetry_title">สถิติการใช้งานแอปพลิเคชัน</string>
    <string name="usage_sessions">จำนวนเซสชัน: %1$d</string>
    <string name="usage_active_time">เวลาที่ใช้งาน: %1$d นาที</string>
    <string name="usage_interactions">การโต้ตอบ: %1$d</string>
""",
    "values-pl": """
    <string name="usage_telemetry_title">STATYSTYKI UŻYCIA APLIKACJI</string>
    <string name="usage_sessions">Sesje: %1$d</string>
    <string name="usage_active_time">Czas aktywności: %1$d min</string>
    <string name="usage_interactions">Interakcje: %1$d</string>
""",
    "values-nl": """
    <string name="usage_telemetry_title">APP GEBRUIKSSTATISTIEKEN</string>
    <string name="usage_sessions">Sessies: %1$d</string>
    <string name="usage_active_time">Actieve tijd: %1$d min</string>
    <string name="usage_interactions">Interacties: %1$d</string>
""",
    "values-uk": """
    <string name="usage_telemetry_title">СТАТИСТИКА ВИКОРИСТАННЯ ДОДАТКУ</string>
    <string name="usage_sessions">Сесії: %1$d</string>
    <string name="usage_active_time">Активний час: %1$d хв</string>
    <string name="usage_interactions">Взаємодії: %1$d</string>
""",
    "values-cs": """
    <string name="usage_telemetry_title">STATISTIKA POUŽITÍ APLIKACE</string>
    <string name="usage_sessions">Relace: %1$d</string>
    <string name="usage_active_time">Aktivní čas: %1$d min</string>
    <string name="usage_interactions">Interakce: %1$d</string>
"""
}

base_dir = "app/src/main/res"

for folder, xml_snippet in telemetry_snippets.items():
    file_path = os.path.join(base_dir, folder, "strings.xml")
    if os.path.exists(file_path):
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
        
        if "</resources>" in content and "usage_telemetry_title" not in content:
            updated = content.replace("</resources>", xml_snippet + "</resources>")
            with open(file_path, "w", encoding="utf-8") as f:
                f.write(updated)
            print(f"Updated {folder}")
        else:
            print(f"Skipped {folder}")
