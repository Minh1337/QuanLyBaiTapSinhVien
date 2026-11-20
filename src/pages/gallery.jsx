import { useState, useMemo, useRef } from "react";
import { useBlog } from "../context/BlogContext";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import { api } from "../services/api";

const GALLERY_TAGS = [
  { id: "all", label: "✨ Tất cả" },
  { id: "desksetup", label: "🖥️ Góc làm việc (#desksetup)" },
  { id: "goclaptrinh", label: "⌨️ Góc lập trình (#goclaptrinh)" },
  { id: "devlife", label: "☕ Đời sống Dev (#devlife)" },
  { id: "chiaseanh", label: "📸 Nhiếp ảnh (#chiaseanh)" },
  { id: "meme", label: "🎭 Meme công nghệ (#meme)" }
];

export default function GalleryPage({ onNavigate }) {
  const { posts, toggleLike, createPost } = useBlog();
  const { currentUser, requireAuth } = useAuth();
  const { addToast } = useToast();

  const [activeTag, setActiveTag] = useState("all");
  const [search, setSearch] = useState("");
  const [selectedItem, setSelectedItem] = useState(null);

  // Modal Đăng ảnh riêng cho Góc Ngoài Lề (Flickr-style)
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [photoTitle, setPhotoTitle] = useState("");
  const [photoCategory, setPhotoCategory] = useState("desksetup");
  const [photoDescription, setPhotoDescription] = useState("");
  const [gearSetup, setGearSetup] = useState("");
  const [photoFile, setPhotoFile] = useState(null);
  const [photoPreview, setPhotoPreview] = useState("");
  const [photoUrlInput, setPhotoUrlInput] = useState("");
  const [imageInputMode, setImageInputMode] = useState("file"); // "file" | "url"
  const [uploading, setUploading] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef(null);

  // Lọc các bài viết thuộc chuyên mục "Ngoài Lề" hoặc có hình ảnh / tag ngoài lề
  const offTopicPosts = useMemo(() => {
    return posts.filter((p) => {
      const isOffTopicCat = (p.category || "").toLowerCase().includes("ngoài lề") || (p.category || "").toLowerCase().includes("ngoai le");
      const hasOffTopicTag = (p.tags || []).some((t) =>
        ["ngoàilề", "ngoai-le", "desksetup", "goclaptrinh", "chiaseanh", "devlife", "meme"].includes(t.toLowerCase().replace("#", ""))
      );
      // Hoặc bài viết có ảnh cover đẹp
      const hasCover = Boolean(p.coverImage || p.cover_image);
      return (isOffTopicCat || hasOffTopicTag) && hasCover;
    });
  }, [posts]);

  // Bộ lọc theo tag và từ khóa tìm kiếm
  const filteredItems = useMemo(() => {
    return offTopicPosts.filter((item) => {
      if (activeTag !== "all") {
        const hasTag = (item.tags || []).some((t) =>
          t.toLowerCase().replace("#", "") === activeTag.toLowerCase()
        );
        if (!hasTag) return false;
      }
      if (search.trim()) {
        const q = search.toLowerCase().trim();
        const titleMatch = (item.title || "").toLowerCase().includes(q);
        const excerptMatch = (item.excerpt || "").toLowerCase().includes(q);
        const authorMatch = (item.author?.name || "").toLowerCase().includes(q);
        if (!titleMatch && !excerptMatch && !authorMatch) return false;
      }
      return true;
    });
  }, [offTopicPosts, activeTag, search]);

  const handleLikePost = (e, postId) => {
    e.stopPropagation();
    requireAuth(
      () => {
        toggleLike(postId);
        addToast("Đã thả tim cho bức ảnh! ❤️", "success");
      },
      "Vui lòng đăng nhập để thích bài viết!"
    );
  };

  const handleShareYourPhoto = () => {
    requireAuth(() => {
      setShowUploadModal(true);
    }, "Vui lòng đăng nhập để đăng ảnh lên Góc ngoài lề!");
  };

  const handleFileSelect = (file) => {
    if (!file) return;
    if (!file.type.startsWith("image/")) {
      addToast("Vui lòng chọn tệp định dạng hình ảnh (JPG, PNG, WebP, GIF)!", "warning");
      return;
    }
    if (file.size > 8 * 1024 * 1024) {
      addToast("Dung lượng ảnh quá lớn (tối đa 8MB)!", "warning");
      return;
    }
    setPhotoFile(file);
    const reader = new FileReader();
    reader.onload = (e) => {
      setPhotoPreview(e.target.result);
    };
    reader.readAsDataURL(file);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      handleFileSelect(files[0]);
    }
  };

  const handleClearSelectedImage = () => {
    setPhotoFile(null);
    setPhotoPreview("");
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleSubmitPhoto = async (e) => {
    e.preventDefault();
    if (!photoTitle.trim()) {
      addToast("Vui lòng nhập tiêu đề bức ảnh hoặc bộ ảnh!", "warning");
      return;
    }

    let finalCoverUrl = "";
    setUploading(true);

    try {
      if (imageInputMode === "file") {
        if (!photoFile && !photoPreview) {
          addToast("Vui lòng chọn hoặc tải lên một tệp ảnh!", "warning");
          setUploading(false);
          return;
        }

        if (photoFile) {
          try {
            const uploadRes = await api.uploads.uploadImage(photoFile);
            if (uploadRes?.url) {
              const rawUrl = uploadRes.url;
              const serverBase = (import.meta.env.VITE_API_URL || "http://localhost:8000/api/v1").replace("/api/v1", "");
              finalCoverUrl = rawUrl.startsWith("http") ? rawUrl : `${serverBase}${rawUrl}`;
            }
          } catch (uploadErr) {
            console.warn("Backend file upload fallback to base64 preview:", uploadErr);
            finalCoverUrl = photoPreview;
          }
        } else {
          finalCoverUrl = photoPreview;
        }
      } else {
        if (!photoUrlInput.trim()) {
          addToast("Vui lòng dán liên kết URL hình ảnh!", "warning");
          setUploading(false);
          return;
        }
        finalCoverUrl = photoUrlInput.trim();
      }

      if (!finalCoverUrl) {
        addToast("Chưa có hình ảnh hợp lệ để đăng tải!", "warning");
        setUploading(false);
        return;
      }

      const tagsList = ["ngoàilề", photoCategory];
      if (gearSetup.trim()) {
        tagsList.push("setup", "gear");
      }

      let markdownContent = `![${photoTitle.trim()}](${finalCoverUrl})\n\n`;
      if (photoDescription.trim()) {
        markdownContent += `${photoDescription.trim()}\n\n`;
      }
      if (gearSetup.trim()) {
        markdownContent += `### ⚙️ Thông số & Trang thiết bị (Gear / Setup)\n${gearSetup.trim()}\n`;
      }

      const postPayload = {
        title: photoTitle.trim(),
        category: "Ngoài Lề",
        tags: tagsList,
        coverImage: finalCoverUrl,
        cover_image: finalCoverUrl,
        excerpt: photoDescription.trim().slice(0, 160) || photoTitle.trim(),
        content: markdownContent,
        status: "approved"
      };

      const created = await createPost(postPayload);
      if (created) {
        addToast("Đã đăng tải ảnh lên Góc ngoài lề thành công! 📸🎉", "success");
        setPhotoTitle("");
        setPhotoDescription("");
        setGearSetup("");
        setPhotoFile(null);
        setPhotoPreview("");
        setPhotoUrlInput("");
        setShowUploadModal(false);
        setActiveTag(photoCategory);
      }
    } catch (err) {
      console.error("Lỗi khi đăng ảnh:", err);
      addToast(err?.message || "Không thể đăng ảnh lúc này. Vui lòng thử lại sau!", "error");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8 space-y-8 animate-fade-in">
      {/* Header Banner phong cách Flickr / Gallery */}
      <div className="p-6 sm:p-10 rounded-3xl bg-gradient-to-r from-amber-500/10 via-base-100 to-rose-500/10 border border-base-300 shadow-sm flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="space-y-2 text-center md:text-left">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/15 text-amber-700 dark:text-amber-400 text-xs font-bold uppercase tracking-wider">
            <span>📸</span> Flickr Dev & Góc Ngoài Lề (#ngoài-lề)
          </div>
          <h1 className="text-2xl sm:text-4xl font-black text-base-content tracking-tight">
            Góc Chia Sẻ Ngoài Lề & Thư Viện Ảnh
          </h1>
          <p className="text-xs sm:text-sm text-base-content/70 max-w-xl">
            Không gian thư giãn dành riêng cho cộng đồng lập trình viên: Khoe góc làm việc (desk setup), bàn phím cơ, góc cà phê săn bug, những bức ảnh đời sống và meme vui vẻ bên ngoài dòng code khô khan!
          </p>
        </div>

        <button
          type="button"
          onClick={handleShareYourPhoto}
          className="btn btn-primary text-white font-bold rounded-2xl shadow-md hover:scale-105 active:scale-95 transition-all text-xs sm:text-sm gap-2 shrink-0 px-6"
        >
          <span>📸</span>
          <span>Đăng ảnh góc làm việc ngay</span>
        </button>
      </div>

      {/* Filter Tabs & Search Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-base-200">
        <div className="flex flex-wrap items-center gap-2">
          {GALLERY_TAGS.map((t) => (
            <button
              key={t.id}
              type="button"
              onClick={() => setActiveTag(t.id)}
              className={`btn btn-xs sm:btn-sm rounded-xl font-bold transition-all ${
                activeTag === t.id
                  ? "btn-primary text-white shadow-xs"
                  : "btn-ghost text-base-content/70 hover:text-base-content"
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>

        <div className="relative w-full sm:w-60">
          <input
            type="text"
            placeholder="Tìm ảnh hoặc tác giả..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="input input-sm input-bordered w-full rounded-xl pl-8 text-xs"
          />
          <span className="absolute left-2.5 top-2 text-xs text-base-content/50">🔍</span>
        </div>
      </div>

      {/* Flickr-Style Masonry Photo Grid */}
      {filteredItems.length === 0 ? (
        <div className="text-center py-16 bg-base-100 rounded-3xl border border-dashed border-base-300 p-8 space-y-3">
          <span className="text-5xl">📷</span>
          <h3 className="font-bold text-base text-base-content">Chưa có bài viết ảnh nào trong mục này</h3>
          <p className="text-xs text-base-content/60 max-w-sm mx-auto">
            Hãy là người đầu tiên đăng tải bộ ảnh góc làm việc hoặc chia sẻ khoảnh khắc đời sống lập trình viên!
          </p>
          <button
            type="button"
            onClick={handleShareYourPhoto}
            className="btn btn-sm btn-primary text-white font-bold rounded-xl"
          >
            📸 Đăng ảnh đầu tiên
          </button>
        </div>
      ) : (
        <div className="columns-1 sm:columns-2 lg:columns-3 gap-6 space-y-6">
          {filteredItems.map((item) => {
            const cover = item.coverImage || item.cover_image;
            const likesCount = Array.isArray(item.likes) ? item.likes.length : (item.likes_count || 0);
            const isLiked = currentUser && Array.isArray(item.likes) && item.likes.some((u) => String(u) === String(currentUser.id));

            return (
              <div
                key={item.id}
                onClick={() => setSelectedItem(item)}
                className="break-inside-avoid card bg-base-100 border border-base-300 rounded-2xl overflow-hidden hover:border-primary/50 hover:shadow-xl transition-all duration-300 cursor-pointer group relative"
              >
                {/* Photo with hover vignette overlay */}
                <div className="relative overflow-hidden bg-base-300">
                  <img
                    src={cover}
                    alt={item.title}
                    className="w-full h-auto object-cover group-hover:scale-105 transition-transform duration-500"
                    loading="lazy"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 p-4 flex flex-col justify-between">
                    <div className="flex justify-end">
                      <button
                        type="button"
                        onClick={(e) => handleLikePost(e, item.id)}
                        className={`btn btn-circle btn-xs ${isLiked ? "btn-error text-white" : "btn-ghost bg-black/40 text-white hover:bg-black/60"}`}
                        title="Thả tim"
                      >
                        ❤️
                      </button>
                    </div>

                    <div className="space-y-1 text-white">
                      <div className="flex items-center gap-1.5 text-xs text-white/80">
                        <span className="badge badge-xs badge-primary text-white font-bold">
                          {item.category}
                        </span>
                        <span>•</span>
                        <span>{item.readTime || "3 phút"}</span>
                      </div>
                      <h3 className="font-bold text-sm leading-snug line-clamp-2 drop-shadow-sm">
                        {item.title}
                      </h3>
                    </div>
                  </div>
                </div>

                {/* Info Bar dưới ảnh */}
                <div className="p-3.5 flex items-center justify-between text-xs border-t border-base-200">
                  <div className="flex items-center gap-2 truncate">
                    <img
                      src={item.author?.avatar || "https://api.dicebear.com/7.x/bottts/svg?seed=dev"}
                      alt={item.author?.name}
                      className="w-6 h-6 rounded-full bg-base-300 object-cover"
                    />
                    <span className="font-semibold text-base-content/80 text-[11px] truncate">
                      {item.author?.name || "Tác giả"}
                    </span>
                  </div>

                  <div className="flex items-center gap-3 text-[11px] text-base-content/60">
                    <span className="flex items-center gap-1">
                      <span>❤️</span>
                      <span>{likesCount}</span>
                    </span>
                    <span className="flex items-center gap-1">
                      <span>💬</span>
                      <span>{item.comments_count || item.comments?.length || 0}</span>
                    </span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Flickr Quick-View Lightbox Modal */}
      {selectedItem && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-xs p-4 animate-fade-in"
          onClick={(e) => { if (e.target === e.currentTarget) setSelectedItem(null); }}
        >
          <div className="relative w-full max-w-4xl bg-base-100 rounded-3xl shadow-2xl border border-base-300 overflow-hidden flex flex-col lg:flex-row max-h-[92vh]">
            {/* Cột hiển thị ảnh lớn */}
            <div className="lg:w-3/5 bg-black/95 flex items-center justify-center p-4 relative min-h-[300px]">
              <img
                src={selectedItem.coverImage || selectedItem.cover_image}
                alt={selectedItem.title}
                className="max-h-[80vh] w-auto max-w-full object-contain rounded-xl"
              />
            </div>

            {/* Cột thông tin & bình luận nhanh */}
            <div className="lg:w-2/5 p-6 flex flex-col justify-between space-y-4 overflow-y-auto">
              <div>
                <div className="flex items-center justify-between pb-3 border-b border-base-200">
                  <div className="flex items-center gap-2">
                    <img
                      src={selectedItem.author?.avatar || "https://api.dicebear.com/7.x/bottts/svg?seed=author"}
                      alt={selectedItem.author?.name}
                      className="w-9 h-9 rounded-full bg-base-300 object-cover border border-base-300"
                    />
                    <div>
                      <h4 className="font-bold text-xs text-base-content">{selectedItem.author?.name || "Tác giả"}</h4>
                      <p className="text-[10px] text-base-content/50">@{selectedItem.author?.username || "dev"}</p>
                    </div>
                  </div>
                  <button
                    onClick={() => setSelectedItem(null)}
                    className="btn btn-sm btn-circle btn-ghost"
                  >
                    ✕
                  </button>
                </div>

                <div className="space-y-3 pt-3">
                  <span className="badge badge-primary text-white font-bold text-xs">
                    {selectedItem.category}
                  </span>
                  <h2 className="text-lg font-black text-base-content leading-snug">
                    {selectedItem.title}
                  </h2>
                  <p className="text-xs text-base-content/75 leading-relaxed whitespace-pre-wrap">
                    {selectedItem.excerpt || selectedItem.content?.slice(0, 200) || "Khoảnh khắc tuyệt vời được chia sẻ trong cộng đồng ngoài lề IT Blog."}
                  </p>

                  <div className="flex flex-wrap gap-1.5 pt-2">
                    {(selectedItem.tags || []).map((t) => (
                      <span key={t} className="badge badge-xs badge-outline">
                        #{t}
                      </span>
                    ))}
                  </div>
                </div>
              </div>

              <div className="pt-4 border-t border-base-200 flex items-center justify-between gap-3">
                <button
                  type="button"
                  onClick={(e) => handleLikePost(e, selectedItem.id)}
                  className="btn btn-sm btn-outline gap-1.5 text-xs font-bold"
                >
                  <span>❤️</span>
                  <span>{Array.isArray(selectedItem.likes) ? selectedItem.likes.length : (selectedItem.likes_count || 0)} Thích</span>
                </button>

                <button
                  type="button"
                  onClick={() => {
                    setSelectedItem(null);
                    onNavigate && onNavigate("post_detail", { postId: selectedItem.id });
                  }}
                  className="btn btn-sm btn-primary text-white font-bold text-xs px-4 rounded-xl"
                >
                  Xem toàn bộ bài viết →
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Flickr-Style Dedicated Photo Upload Modal */}
      {showUploadModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-xs p-4 animate-fade-in"
          onClick={(e) => {
            if (!uploading && e.target === e.currentTarget) setShowUploadModal(false);
          }}
        >
          <div className="relative w-full max-w-2xl bg-base-100 rounded-3xl shadow-2xl border border-base-300 overflow-hidden flex flex-col max-h-[92vh]">
            {/* Header Modal */}
            <div className="p-5 border-b border-base-200 flex items-center justify-between bg-gradient-to-r from-amber-500/10 via-rose-500/10 to-transparent">
              <div className="flex items-center gap-2.5">
                <span className="w-10 h-10 rounded-2xl bg-gradient-to-br from-amber-500 to-rose-500 text-white flex items-center justify-center text-xl font-bold shadow-xs">
                  📸
                </span>
                <div>
                  <div className="flex items-center gap-2">
                    <h3 className="font-black text-base sm:text-lg text-base-content">
                      Đăng Ảnh Lên Góc Ngoài Lề
                    </h3>
                    <span className="badge badge-sm badge-warning text-amber-950 font-bold">
                      Flickr Dev
                    </span>
                  </div>
                  <p className="text-[11px] text-base-content/60">
                    Khoe góc setup, bàn phím cơ, đời sống lập trình hoặc meme vui vẻ
                  </p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => !uploading && setShowUploadModal(false)}
                disabled={uploading}
                className="btn btn-sm btn-circle btn-ghost"
              >
                ✕
              </button>
            </div>

            {/* Form body */}
            <form onSubmit={handleSubmitPhoto} className="p-5 space-y-4 overflow-y-auto">
              {/* Tabs chuyển đổi nguồn ảnh: Tải từ máy vs Link Online */}
              <div>
                <label className="block text-xs font-bold text-base-content/80 mb-1.5">
                  Hình ảnh bài đăng <span className="text-error">*</span>
                </label>
                <div className="join w-full mb-3">
                  <button
                    type="button"
                    onClick={() => setImageInputMode("file")}
                    className={`join-item btn btn-xs sm:btn-sm flex-1 font-bold ${
                      imageInputMode === "file" ? "btn-primary text-white" : "btn-ghost border border-base-300"
                    }`}
                  >
                    📁 Tải ảnh từ máy tính
                  </button>
                  <button
                    type="button"
                    onClick={() => setImageInputMode("url")}
                    className={`join-item btn btn-xs sm:btn-sm flex-1 font-bold ${
                      imageInputMode === "url" ? "btn-primary text-white" : "btn-ghost border border-base-300"
                    }`}
                  >
                    🌐 Dán link ảnh Online
                  </button>
                </div>

                {/* Chế độ 1: Tải ảnh từ máy tính */}
                {imageInputMode === "file" && (
                  <div>
                    <input
                      ref={fileInputRef}
                      type="file"
                      accept="image/*"
                      onChange={(e) => handleFileSelect(e.target.files?.[0])}
                      className="hidden"
                    />

                    {photoPreview ? (
                      <div className="relative rounded-2xl border-2 border-primary/40 bg-base-200/50 p-2 overflow-hidden group">
                        <img
                          src={photoPreview}
                          alt="Preview"
                          className="w-full max-h-64 object-contain rounded-xl mx-auto"
                        />
                        <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                          <button
                            type="button"
                            onClick={() => fileInputRef.current?.click()}
                            className="btn btn-sm btn-neutral text-white font-bold rounded-xl"
                          >
                            Đổi ảnh khác
                          </button>
                          <button
                            type="button"
                            onClick={handleClearSelectedImage}
                            className="btn btn-sm btn-error text-white font-bold rounded-xl"
                          >
                            Xóa ảnh
                          </button>
                        </div>
                      </div>
                    ) : (
                      <div
                        onClick={() => fileInputRef.current?.click()}
                        onDragOver={handleDragOver}
                        onDragLeave={handleDragLeave}
                        onDrop={handleDrop}
                        className={`border-2 border-dashed rounded-2xl p-6 text-center cursor-pointer transition-all flex flex-col items-center justify-center gap-2 ${
                          isDragging
                            ? "border-primary bg-primary/10 scale-[1.01]"
                            : "border-base-300 hover:border-primary/60 bg-base-200/30 hover:bg-base-200/60"
                        }`}
                      >
                        <div className="w-12 h-12 rounded-2xl bg-primary/10 text-primary flex items-center justify-center text-2xl">
                          📷
                        </div>
                        <div>
                          <p className="text-xs sm:text-sm font-bold text-base-content">
                            Kéo thả ảnh vào đây hoặc <span className="text-primary underline">bấm để chọn ảnh</span>
                          </p>
                          <p className="text-[11px] text-base-content/50 mt-0.5">
                            Hỗ trợ JPG, PNG, GIF, WebP (Dung lượng tối đa 8MB)
                          </p>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* Chế độ 2: Dán đường dẫn URL */}
                {imageInputMode === "url" && (
                  <div className="space-y-2">
                    <input
                      type="url"
                      value={photoUrlInput}
                      onChange={(e) => setPhotoUrlInput(e.target.value)}
                      placeholder="https://images.unsplash.com/photo-... hoặc link Flickr, Imgur"
                      className="input input-sm input-bordered w-full text-xs rounded-xl focus:input-primary"
                    />
                    {photoUrlInput && (
                      <div className="rounded-xl border border-base-300 p-2 bg-base-200/40">
                        <img
                          src={photoUrlInput}
                          alt="Preview URL"
                          onError={(e) => { e.target.style.display = 'none'; }}
                          className="w-full max-h-48 object-contain rounded-lg"
                        />
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* Tiêu đề bài đăng ảnh */}
              <div>
                <label className="block text-xs font-bold text-base-content/80 mb-1">
                  Tiêu đề bức ảnh / Bộ ảnh <span className="text-error">*</span>
                </label>
                <input
                  type="text"
                  value={photoTitle}
                  onChange={(e) => setPhotoTitle(e.target.value)}
                  placeholder="Ví dụ: Góc làm việc tối giản đón năm mới 2026, Phím cơ custom 75%..."
                  className="input input-sm input-bordered w-full text-xs rounded-xl focus:input-primary font-semibold"
                  required
                />
              </div>

              {/* Chọn chủ đề ngoài lề (Pill options) */}
              <div>
                <label className="block text-xs font-bold text-base-content/80 mb-1.5">
                  Chủ đề góc ngoài lề:
                </label>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                  {GALLERY_TAGS.filter((t) => t.id !== "all").map((tag) => (
                    <button
                      key={tag.id}
                      type="button"
                      onClick={() => setPhotoCategory(tag.id)}
                      className={`btn btn-xs sm:btn-sm rounded-xl font-bold transition-all text-[11px] sm:text-xs justify-start ${
                        photoCategory === tag.id
                          ? "btn-primary text-white shadow-xs"
                          : "btn-outline border-base-300 text-base-content/80"
                      }`}
                    >
                      {tag.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Mô tả / Cảm nghĩ */}
              <div>
                <label className="block text-xs font-bold text-base-content/80 mb-1">
                  Mô tả & Câu chuyện đằng sau bức ảnh (Tùy chọn)
                </label>
                <textarea
                  value={photoDescription}
                  onChange={(e) => setPhotoDescription(e.target.value)}
                  placeholder="Chia sẻ một chút về cảm xúc, góc setup này đã giúp bạn hoàn thành dự án nào, hoặc mẹo sắp xếp góc làm việc..."
                  className="textarea textarea-bordered textarea-sm w-full text-xs rounded-xl focus:textarea-primary"
                  rows={2}
                />
              </div>

              {/* Thông số Gear / Thiết bị góc setup */}
              <div>
                <label className="block text-xs font-bold text-base-content/80 mb-1">
                  ⚙️ Thông số thiết bị / Gear (Dành cho góc làm việc & phím cơ)
                </label>
                <input
                  type="text"
                  value={gearSetup}
                  onChange={(e) => setGearSetup(e.target.value)}
                  placeholder="Ví dụ: Màn Dell U2723QE, Keychron Q1, Chuột MX Master 3S, Bàn nâng hạ..."
                  className="input input-sm input-bordered w-full text-xs rounded-xl focus:input-primary"
                />
              </div>

              {/* Buttons footer */}
              <div className="pt-3 border-t border-base-200 flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowUploadModal(false)}
                  disabled={uploading}
                  className="btn btn-sm btn-ghost rounded-xl"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={uploading || !photoTitle.trim() || (!photoPreview && !photoUrlInput.trim())}
                  className="btn btn-sm btn-primary text-white font-bold gap-1.5 rounded-xl shadow-md px-5"
                >
                  {uploading ? (
                    <>
                      <span className="loading loading-spinner loading-xs"></span>
                      <span>Đang đăng ảnh...</span>
                    </>
                  ) : (
                    <>
                      <span>📸</span>
                      <span>Đăng ảnh lên Góc Ngoài Lề</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
