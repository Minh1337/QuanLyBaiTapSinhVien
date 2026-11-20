import { useState, useEffect, useMemo } from "react";
import { useAuth } from "../context/AuthContext";
import { useBlog } from "../context/BlogContext";
import { useToast } from "../context/ToastContext";
import { api } from "../services/api";

const SEED_SERIES = [
  {
    id: 1,
    title: "Lộ Trình Tinh Thông Fullstack Modern Web 2026",
    slug: "lo-trinh-tinh-thong-fullstack-modern-web-2026",
    description: "Tuyển tập các bài viết thực chiến từ xây dựng Backend FastAPI chịu tải cao, Microservices đến tối ưu giao diện Frontend với React 19 và Tailwind v4.",
    cover_image: "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=1000&auto=format&fit=crop&q=80",
    author: {
      id: "1",
      name: "Quản Trị Viên IT Blog",
      username: "admin",
      avatar: "https://api.dicebear.com/7.x/bottts/svg?seed=admin"
    },
    posts_count: 2,
    items: [
      { id: 1, series_id: 1, post_id: 1, order_index: 1, post_title: "Xây Dựng Hệ Thống Kiến Trúc Microservices Với FastAPI Và PostgreSQL", post_read_time: "8 phút đọc", post_views: 1420 },
      { id: 2, series_id: 1, post_id: 2, order_index: 2, post_title: "Tối Ưu Hóa Giao Diện Người Dùng Với React 19 Và Tailwind CSS v4", post_read_time: "5 phút đọc", post_views: 981 }
    ],
    created_at: "2026-10-14T14:11:29.363Z"
  }
];

export default function SeriesPage({ onNavigate, params = {} }) {
  const { currentUser, requireAuth } = useAuth();
  const { posts } = useBlog();
  const { addToast } = useToast();

  const [seriesList, setSeriesList] = useState(SEED_SERIES);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedSeries, setSelectedSeries] = useState(null);

  // Modal Create/Edit Series
  const [showModal, setShowModal] = useState(params?.action === "create");
  const [editingSeries, setEditingSeries] = useState(null);
  const [modalTitle, setModalTitle] = useState("");
  const [modalDesc, setModalDesc] = useState("");
  const [modalCover, setModalCover] = useState("");
  const [selectedPostIds, setSelectedPostIds] = useState([]);
  const [saving, setSaving] = useState(false);

  // Load series from backend
  const loadSeries = async () => {
    setLoading(true);
    try {
      const data = await api.series.list();
      if (Array.isArray(data) && data.length > 0) {
        setSeriesList(data);
      }
    } catch {
      // Keep seed series
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSeries();
  }, []);

  // Filter series
  const filteredSeries = useMemo(() => {
    if (!searchQuery.trim()) return seriesList;
    const q = searchQuery.toLowerCase().trim();
    return seriesList.filter((s) =>
      (s.title || "").toLowerCase().includes(q) ||
      (s.description || "").toLowerCase().includes(q)
    );
  }, [seriesList, searchQuery]);

  // User's own posts for the selector
  const myPosts = useMemo(() => {
    if (!currentUser) return [];
    return posts.filter((p) => String(p.authorId || p.author?.id) === String(currentUser.id) || currentUser.role === "admin");
  }, [posts, currentUser]);

  const handleOpenCreateModal = () => {
    requireAuth(() => {
      setEditingSeries(null);
      setModalTitle("");
      setModalDesc("");
      setModalCover("");
      setSelectedPostIds(myPosts.slice(0, 3).map((p) => Number(p.id)));
      setShowModal(true);
    }, "Vui lòng đăng nhập để tạo Chuỗi bài viết!");
  };

  const handleOpenEditModal = (s) => {
    setEditingSeries(s);
    setModalTitle(s.title);
    setModalDesc(s.description || "");
    setModalCover(s.cover_image || "");
    setSelectedPostIds(s.items ? s.items.map((i) => i.post_id) : []);
    setShowModal(true);
  };

  const handleToggleSelectPost = (pId) => {
    const idNum = Number(pId);
    setSelectedPostIds((prev) =>
      prev.includes(idNum) ? prev.filter((id) => id !== idNum) : [...prev, idNum]
    );
  };

  const handleMoveOrder = (index, direction) => {
    const targetIdx = index + direction;
    if (targetIdx < 0 || targetIdx >= selectedPostIds.length) return;
    const newArr = [...selectedPostIds];
    const temp = newArr[index];
    newArr[index] = newArr[targetIdx];
    newArr[targetIdx] = temp;
    setSelectedPostIds(newArr);
  };

  const handleSaveSeries = async (e) => {
    e.preventDefault();
    if (!modalTitle.trim()) {
      addToast("Vui lòng nhập tiêu đề chuỗi bài viết!", "warning");
      return;
    }
    if (selectedPostIds.length === 0) {
      addToast("Vui lòng chọn ít nhất 1 bài viết để đưa vào chuỗi!", "warning");
      return;
    }

    setSaving(true);
    try {
      const payload = {
        title: modalTitle.trim(),
        description: modalDesc.trim() || undefined,
        cover_image: modalCover.trim() || "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=1000&q=80",
        post_ids: selectedPostIds
      };

      if (editingSeries) {
        const res = await api.series.update(editingSeries.id, payload);
        addToast("Cập nhật chuỗi bài viết thành công! 🎉", "success");
        if (selectedSeries?.id === editingSeries.id) setSelectedSeries(res);
      } else {
        const res = await api.series.create(payload);
        addToast("Tạo chuỗi bài viết mới thành công! 📚", "success");
        setSelectedSeries(res);
      }
      setShowModal(false);
      loadSeries();
    } catch (err) {
      addToast(err.message || "Lỗi lưu chuỗi bài viết.", "error");
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteSeries = async (sId) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa chuỗi bài viết này? (Các bài viết gốc sẽ không bị xóa)")) return;
    try {
      await api.series.delete(sId);
      addToast("Đã xóa chuỗi bài viết thành công!", "success");
      if (selectedSeries?.id === sId) setSelectedSeries(null);
      loadSeries();
    } catch (err) {
      addToast(err.message || "Không thể xóa chuỗi bài viết.", "error");
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-8 animate-fade-in">
      {/* Header Banner */}
      <div className="p-6 sm:p-10 rounded-3xl bg-gradient-to-r from-primary/10 via-base-100 to-secondary/10 border border-base-300 shadow-sm flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="space-y-2 text-center md:text-left">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-primary/15 text-primary text-xs font-bold uppercase tracking-wider">
            <span>📚</span> Tuyển tập chuyên đề (Series)
          </div>
          <h1 className="text-2xl sm:text-4xl font-black text-base-content tracking-tight">
            Chuỗi Bài Viết Chọn Lọc
          </h1>
          <p className="text-xs sm:text-sm text-base-content/70 max-w-xl">
            Tự do gom ghép các bài viết tâm đắc của bạn thành một chuỗi bài liền mạch theo thứ tự (Bài 1, Bài 2...). Giúp độc giả dễ dàng học tập và theo dõi lộ trình từ đầu đến cuối!
          </p>
        </div>

        <button
          type="button"
          onClick={handleOpenCreateModal}
          className="btn btn-primary text-white font-bold rounded-2xl shadow-md hover:scale-105 active:scale-95 transition-all text-xs sm:text-sm gap-2 shrink-0 px-6"
        >
          <span>➕</span>
          <span>Tạo chuỗi bài viết</span>
        </button>
      </div>

      {/* Selected Series Detail View */}
      {selectedSeries ? (
        <div className="space-y-6 animate-fade-in bg-base-100 p-6 sm:p-8 rounded-3xl border border-base-300 shadow-sm">
          <button
            type="button"
            onClick={() => setSelectedSeries(null)}
            className="btn btn-xs btn-ghost gap-1.5 text-xs font-semibold hover:text-primary"
          >
            <span>←</span> Quay lại danh sách chuỗi bài
          </button>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
            <div className="w-full h-48 md:h-64 rounded-2xl overflow-hidden bg-base-300 border border-base-300">
              <img
                src={selectedSeries.cover_image || "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=800&q=80"}
                alt={selectedSeries.title}
                className="w-full h-full object-cover"
                onError={(e) => { e.currentTarget.src = "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=800&q=80"; }}
              />
            </div>

            <div className="md:col-span-2 space-y-3">
              <div className="flex items-center gap-2">
                <span className="badge badge-primary text-white font-bold text-xs">
                  {selectedSeries.posts_count || selectedSeries.items?.length || 0} bài viết
                </span>
                <span className="text-xs text-base-content/50">
                  Cập nhật: {new Date(selectedSeries.updated_at || selectedSeries.created_at || Date.now()).toLocaleDateString("vi-VN")}
                </span>
              </div>

              <h2 className="text-2xl sm:text-3xl font-black text-base-content leading-snug">
                {selectedSeries.title}
              </h2>

              <p className="text-sm text-base-content/80 leading-relaxed whitespace-pre-wrap">
                {selectedSeries.description || "Chưa có mô tả cho chuỗi bài viết này."}
              </p>

              {/* Tác giả */}
              <div className="flex items-center gap-3 pt-3 border-t border-base-200">
                <img
                  src={selectedSeries.author?.avatar || "https://api.dicebear.com/7.x/bottts/svg?seed=author"}
                  alt={selectedSeries.author?.name}
                  className="w-9 h-9 rounded-full bg-base-300 object-cover border border-base-300"
                />
                <div>
                  <p className="text-xs font-bold text-base-content">{selectedSeries.author?.name || "Tác giả IT"}</p>
                  <p className="text-[11px] text-base-content/50">@{selectedSeries.author?.username || "author"}</p>
                </div>

                {/* Nút sửa/xóa nếu là tác giả */}
                {currentUser && (String(currentUser.id) === String(selectedSeries.author?.id) || currentUser.role === "admin") && (
                  <div className="ml-auto flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => handleOpenEditModal(selectedSeries)}
                      className="btn btn-xs btn-outline rounded-lg text-xs"
                    >
                      ✏️ Chỉnh sửa
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDeleteSeries(selectedSeries.id)}
                      className="btn btn-xs btn-error text-white rounded-lg text-xs"
                    >
                      🗑️ Xóa
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Danh sách các bài viết trong chuỗi */}
          <div className="pt-6 border-t border-base-200 space-y-4">
            <h3 className="text-lg font-bold text-base-content flex items-center gap-2">
              <span>📑 Các bài viết trong chuỗi theo thứ tự đọc</span>
            </h3>

            <div className="space-y-2.5">
              {selectedSeries.items && selectedSeries.items.length > 0 ? (
                selectedSeries.items.map((item, idx) => (
                  <div
                    key={item.id}
                    onClick={() => onNavigate && onNavigate("post_detail", { postId: item.post_id })}
                    className="p-4 rounded-2xl bg-base-200/50 hover:bg-base-200 border border-base-200 hover:border-primary/40 transition-all cursor-pointer flex items-center justify-between gap-4 group"
                  >
                    <div className="flex items-center gap-3.5 min-w-0">
                      <span className="w-8 h-8 rounded-xl bg-primary/10 text-primary font-mono font-black text-xs flex items-center justify-center shrink-0">
                        #{idx + 1}
                      </span>
                      <div className="min-w-0">
                        <h4 className="font-bold text-sm text-base-content group-hover:text-primary transition-colors truncate">
                          {item.post_title}
                        </h4>
                        <div className="flex items-center gap-2 text-[11px] text-base-content/50 mt-0.5">
                          <span>⏱️ {item.post_read_time || "5 phút đọc"}</span>
                          <span>•</span>
                          <span>👁️ {item.post_views || 0} lượt xem</span>
                        </div>
                      </div>
                    </div>
                    <span className="btn btn-xs btn-primary text-white font-bold rounded-lg shrink-0 group-hover:translate-x-1 transition-transform">
                      Đọc bài này →
                    </span>
                  </div>
                ))
              ) : (
                <p className="text-xs text-base-content/50 italic py-4">Chưa có bài viết nào trong chuỗi này.</p>
              )}
            </div>
          </div>
        </div>
      ) : (
        /* Danh sách Series */
        <div className="space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <h2 className="text-xl font-extrabold text-base-content flex items-center gap-2">
              <span>Khám phá chuỗi bài viết ({filteredSeries.length})</span>
            </h2>

            <div className="relative w-full sm:w-64">
              <input
                type="text"
                placeholder="Tìm chuỗi bài..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="input input-sm input-bordered w-full rounded-xl pl-8 text-xs"
              />
              <span className="absolute left-2.5 top-2 text-xs text-base-content/50">🔍</span>
            </div>
          </div>

          {loading ? (
            <div className="text-center py-16">
              <span className="loading loading-spinner loading-md text-primary"></span>
              <p className="text-xs text-base-content/50 mt-2">Đang tải danh sách chuỗi bài viết...</p>
            </div>
          ) : filteredSeries.length === 0 ? (
            <div className="text-center py-16 bg-base-100 rounded-3xl border border-dashed border-base-300 p-8 space-y-3">
              <span className="text-4xl">📚</span>
              <h3 className="font-bold text-base text-base-content">Chưa tìm thấy chuỗi bài viết nào</h3>
              <p className="text-xs text-base-content/60 max-w-sm mx-auto">
                Hãy là người đầu tiên ghép các bài viết của mình thành một tuyển tập chuyên đề hữu ích!
              </p>
              <button
                type="button"
                onClick={handleOpenCreateModal}
                className="btn btn-sm btn-primary text-white font-bold rounded-xl"
              >
                ➕ Tạo chuỗi bài viết ngay
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredSeries.map((s) => (
                <div
                  key={s.id}
                  onClick={() => setSelectedSeries(s)}
                  className="card bg-base-100 border border-base-300 rounded-2xl overflow-hidden hover:border-primary/50 hover:shadow-lg transition-all duration-300 cursor-pointer flex flex-col justify-between group"
                >
                  <div className="relative h-44 overflow-hidden bg-base-300">
                    <img
                      src={s.cover_image || "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=600&q=80"}
                      alt={s.title}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                      onError={(e) => { e.currentTarget.src = "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=600&q=80"; }}
                    />
                    <div className="absolute top-2.5 right-2.5">
                      <span className="badge badge-primary text-white font-bold text-xs shadow-md">
                        📚 {s.posts_count || s.items?.length || 0} bài viết
                      </span>
                    </div>
                  </div>

                  <div className="p-5 flex-1 flex flex-col justify-between space-y-3">
                    <div className="space-y-1.5">
                      <h3 className="font-bold text-base text-base-content group-hover:text-primary transition-colors line-clamp-2 leading-snug">
                        {s.title}
                      </h3>
                      <p className="text-xs text-base-content/70 line-clamp-2 leading-relaxed">
                        {s.description || "Chuỗi bài viết được tuyển chọn và sắp xếp liền mạch."}
                      </p>
                    </div>

                    <div className="flex items-center justify-between pt-3 border-t border-base-200 text-xs">
                      <div className="flex items-center gap-2">
                        <img
                          src={s.author?.avatar || "https://api.dicebear.com/7.x/bottts/svg?seed=author"}
                          alt={s.author?.name}
                          className="w-6 h-6 rounded-full bg-base-300 object-cover"
                        />
                        <span className="font-semibold text-base-content/80 text-[11px] truncate max-w-[120px]">
                          {s.author?.name || "Tác giả"}
                        </span>
                      </div>
                      <span className="text-primary font-bold text-xs group-hover:underline">Xem chuỗi →</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Modal Tạo/Sửa Series */}
      {showModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/65 backdrop-blur-xs p-4 animate-fade-in"
          onClick={(e) => { if (e.target === e.currentTarget) setShowModal(false); }}
        >
          <div className="relative w-full max-w-2xl bg-base-100 rounded-3xl shadow-2xl border border-base-300 p-6 sm:p-8 max-h-[90vh] overflow-y-auto">
            <button
              onClick={() => setShowModal(false)}
              className="btn btn-sm btn-circle btn-ghost absolute right-4 top-4"
            >
              ✕
            </button>

            <div className="flex items-center gap-2.5 mb-5 pb-3 border-b border-base-200">
              <span className="text-2xl">📚</span>
              <div>
                <h3 className="text-lg font-black text-base-content">
                  {editingSeries ? "Chỉnh sửa Chuỗi bài viết" : "Tạo Chuỗi bài viết mới (Series)"}
                </h3>
                <p className="text-xs text-base-content/60">
                  Ghép nhiều bài viết kỹ thuật lại với nhau để tạo thành tuyển tập học tập mạch lạc.
                </p>
              </div>
            </div>

            <form onSubmit={handleSaveSeries} className="space-y-4">
              <div>
                <label className="label font-bold text-xs text-base-content/80 pb-1">
                  Tiêu đề chuỗi bài viết <span className="text-error">*</span>
                </label>
                <input
                  type="text"
                  value={modalTitle}
                  onChange={(e) => setModalTitle(e.target.value)}
                  placeholder="Ví dụ: Tự Học Microservices Từ Zero Đến Hero"
                  className="input input-sm input-bordered w-full rounded-xl text-xs font-semibold focus:input-primary"
                  required
                />
              </div>

              <div>
                <label className="label font-bold text-xs text-base-content/80 pb-1">
                  Mô tả nội dung chuỗi bài
                </label>
                <textarea
                  value={modalDesc}
                  onChange={(e) => setModalDesc(e.target.value)}
                  placeholder="Mô tả mục tiêu của chuỗi bài viết, người đọc sẽ đạt được kiến thức gì sau khi đọc hết chuỗi..."
                  rows={3}
                  className="textarea textarea-bordered w-full text-xs rounded-xl focus:textarea-primary"
                />
              </div>

              <div>
                <label className="label font-bold text-xs text-base-content/80 pb-1">
                  URL Ảnh bìa chuỗi bài (Cover Image)
                </label>
                <input
                  type="url"
                  value={modalCover}
                  onChange={(e) => setModalCover(e.target.value)}
                  placeholder="https://images.unsplash.com/photo-..."
                  className="input input-sm input-bordered w-full rounded-xl text-xs focus:input-primary"
                />
              </div>

              {/* Bộ chọn bài viết và sắp xếp thứ tự */}
              <div className="space-y-2 pt-2 border-t border-base-200">
                <div className="flex items-center justify-between">
                  <label className="font-bold text-xs text-base-content flex items-center gap-1.5">
                    <span>📑 Chọn và sắp xếp các bài viết trong chuỗi</span>
                    <span className="badge badge-xs badge-primary text-white font-bold">
                      Đã chọn: {selectedPostIds.length} bài
                    </span>
                  </label>
                </div>

                {/* Danh sách các bài đã chọn với nút xếp thứ tự Lên/Xuống */}
                {selectedPostIds.length > 0 && (
                  <div className="p-3 bg-base-200/50 rounded-2xl border border-base-300 space-y-2">
                    <p className="text-[11px] font-bold text-base-content/70">
                      Thứ tự đọc (Dùng nút ⬆️ ⬇️ để thay đổi thứ tự):
                    </p>
                    <div className="space-y-1.5 max-h-40 overflow-y-auto pr-1">
                      {selectedPostIds.map((pId, idx) => {
                        const matched = posts.find((p) => Number(p.id) === Number(pId));
                        return (
                          <div
                            key={pId}
                            className="flex items-center justify-between p-2 rounded-xl bg-base-100 border border-base-300 text-xs"
                          >
                            <div className="flex items-center gap-2 truncate">
                              <span className="w-5 text-center font-mono font-bold text-primary">#{idx + 1}</span>
                              <span className="truncate font-semibold">{matched?.title || `Bài viết #${pId}`}</span>
                            </div>
                            <div className="flex items-center gap-1 shrink-0">
                              <button
                                type="button"
                                disabled={idx === 0}
                                onClick={() => handleMoveOrder(idx, -1)}
                                className="btn btn-xs btn-ghost btn-circle"
                                title="Di chuyển lên trên"
                              >
                                ⬆️
                              </button>
                              <button
                                type="button"
                                disabled={idx === selectedPostIds.length - 1}
                                onClick={() => handleMoveOrder(idx, 1)}
                                className="btn btn-xs btn-ghost btn-circle"
                                title="Di chuyển xuống dưới"
                              >
                                ⬇️
                              </button>
                              <button
                                type="button"
                                onClick={() => handleToggleSelectPost(pId)}
                                className="btn btn-xs btn-ghost text-error"
                                title="Bỏ chọn bài này"
                              >
                                ✕
                              </button>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}

                {/* Danh sách bài viết sẵn có để tích chọn */}
                <div className="space-y-1 pt-1">
                  <p className="text-[11px] text-base-content/60">Tích chọn bài viết để thêm vào chuỗi:</p>
                  <div className="max-h-44 overflow-y-auto space-y-1.5 pr-1 border border-base-200 rounded-xl p-2 bg-base-100">
                    {(myPosts.length > 0 ? myPosts : posts.slice(0, 8)).map((p) => {
                      const isChecked = selectedPostIds.includes(Number(p.id));
                      return (
                        <label
                          key={p.id}
                          className={`flex items-center gap-2.5 p-2 rounded-lg cursor-pointer transition-colors text-xs ${
                            isChecked ? "bg-primary/10 border border-primary/30" : "hover:bg-base-200"
                          }`}
                        >
                          <input
                            type="checkbox"
                            checked={isChecked}
                            onChange={() => handleToggleSelectPost(p.id)}
                            className="checkbox checkbox-xs checkbox-primary rounded"
                          />
                          <span className="truncate flex-1 font-medium">{p.title}</span>
                          <span className="text-[10px] text-base-content/50 shrink-0">{p.category}</span>
                        </label>
                      );
                    })}
                  </div>
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-4 border-t border-base-200">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="btn btn-sm btn-ghost rounded-xl text-xs"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="btn btn-sm btn-primary text-white font-bold rounded-xl px-5 shadow-sm text-xs"
                >
                  {saving ? "Đang lưu..." : editingSeries ? "Cập nhật chuỗi" : "Tạo chuỗi ngay"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
